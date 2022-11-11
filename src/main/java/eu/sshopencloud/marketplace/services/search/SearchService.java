package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorExternalIdMapper;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.mappers.items.ItemContributorMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.PropertyMapper;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.search.SearchActorRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchConceptRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.dto.search.SuggestedSearchPhrases;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.search.filter.*;
import eu.sshopencloud.marketplace.services.search.query.ActorSearchQueryPhrase;
import eu.sshopencloud.marketplace.services.search.query.ConceptSearchQueryPhrase;
import eu.sshopencloud.marketplace.services.search.query.ItemSearchQueryPhrase;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final SearchItemRepository searchItemRepository;
    private final ItemContributorService itemContributorService;
    private final PropertyService propertyService;
    private final SearchConceptRepository searchConceptRepository;
    private final PropertyTypeService propertyTypeService;
    private final SearchActorRepository searchActorRepository;
    private final ActorService actorService;

    public PaginatedSearchItems searchItems(String q, boolean advanced, boolean includeSteps,
                                            @NotNull Map<String, String> expressionParams,
                                            List<ItemCategory> categories, @NotNull Map<String, List<String>> filterParams,
                                            List<SearchOrder> order, PageCoords pageCoords) throws IllegalFilterException {

        log.debug("filterParams " + filterParams.toString());
        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()); // SOLR counts from page 0

        SearchQueryCriteria queryCriteria = new ItemSearchQueryPhrase(q, advanced);

        List<SearchFilterCriteria> filterCriteria = new ArrayList<>();
        filterCriteria.add(makeCategoryCriteria(categories));
        filterCriteria.addAll(makeFiltersCriteria(filterParams, IndexType.ITEMS));

        List<SearchExpressionCriteria> expressionCriteria = makeExpressionCriteria(expressionParams);

        if (order == null || order.isEmpty()) {
            order = Collections.singletonList(SearchOrder.SCORE);
        }

        User currentUser = LoggedInUserHolder.getLoggedInUser();

        FacetPage<IndexItem> facetPage = searchItemRepository.findByQueryAndFilters(queryCriteria, expressionCriteria,
                currentUser, includeSteps, filterCriteria, order, pageable);

        Map<ItemCategory, LabeledCheckedCount> categoryFacet = gatherCategoryFacet(facetPage, categories);
        Map<String, Map<String, CheckedCount>> facets = gatherSearchItemFacets(facetPage, filterParams);

        PaginatedSearchItems result = PaginatedSearchItems.builder()
                .q(q)
                .order(order)
                .items(
                        facetPage.get()
                                .map(SearchConverter::convertIndexItem)
                                .collect(Collectors.toList())
                )
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements())
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages(facetPage.getTotalPages())
                .categories(categoryFacet)
                .facets(facets)
                .build();

        // TODO index contributors and properties directly in SOLR in nested docs (?) -
        // TODO in a similar way add external identifiers to the result
        for (SearchItem searchItem : result.getItems()) {
            searchItem.setContributors(ItemContributorMapper.INSTANCE.toDto(itemContributorService.getItemContributors(searchItem.getId())));
            searchItem.setProperties(PropertyMapper.INSTANCE.toDto(propertyService.getItemProperties(searchItem.getId())));
            searchItem.getProperties().stream().map(PropertyDto::getType).forEach(propertyTypeService::completePropertyType);
        }

        return result;
    }

    private Map<ItemCategory, LabeledCheckedCount> gatherCategoryFacet(FacetPage<IndexItem> facetPage, List<ItemCategory> categories) {
        Map<ItemCategory, LabeledCheckedCount> countedCategories = facetPage.getFacetFields().stream()
                .filter(field -> field.getName().equals(IndexItem.CATEGORY_FIELD))
                .map(facetPage::getFacetResultPage)
                .flatMap(facetFieldEntries -> facetFieldEntries.getContent().stream())
                .collect(
                        Collectors.toMap(
                                facet -> ItemCategoryConverter.convertCategory(facet.getValue()),
                                facet -> SearchConverter.convertCategoryFacet(facet, categories)
                        )
                );

        Map<ItemCategory, LabeledCheckedCount> categoryFacets = new LinkedHashMap<>();
        Arrays.stream(ItemCategory.indexedCategories())
                .forEachOrdered(category ->
                        categoryFacets.put(
                                category,
                                resolveCategoryCount(category, categories, countedCategories)
                        )
                );

        return categoryFacets;
    }

    private LabeledCheckedCount resolveCategoryCount(ItemCategory category, List<ItemCategory> categories, Map<ItemCategory, LabeledCheckedCount> counted) {
        if (counted.containsKey(category))
            return counted.get(category);

        return SearchConverter.convertCategoryFacet(category, 0, categories);
    }

    private Map<String, Map<String, CheckedCount>> gatherSearchItemFacets(FacetPage<IndexItem> facetPage, Map<String, List<String>> filterParams) {
        return facetPage.getFacetFields().stream()
                .filter(field -> !field.getName().equals(IndexItem.CATEGORY_FIELD))
                .map(field -> createFacetsDetails(field.getName(), facetPage, filterParams))
                .collect(Collectors.toMap(
                        Pair::getKey, Pair::getValue,
                        (u, v) -> u,
                        LinkedHashMap::new
                ));
    }


    private static Pair<String, Map<String, CheckedCount>> createFacetsDetails(String fieldName, FacetPage<IndexItem> facetPage, Map<String, List<String>> filterParams) {
        String facetName;
        if (fieldName.startsWith(SearchExpressionDynamicFieldCriteria.DYNAMIC_FIELD_PREFIX)) {
            facetName = fieldName.replace(SearchExpressionDynamicFieldCriteria.DYNAMIC_FIELD_PREFIX, "");
            facetName = facetName.substring(0, facetName.lastIndexOf("_"));
        } else {
            facetName = fieldName.replace('_', '-');
        }
        return Pair.create(
                facetName,
                createFacetDetails(
                        facetPage.getFacetResultPage(fieldName).getContent(),
                        filterParams.get(facetName)
                ));
    }

    private static Map<String, CheckedCount> createFacetDetails(List<FacetFieldEntry> facetValues, List<String> checkedValues) {
        if (checkedValues != null) {
            return facetValues.stream()
                    .collect(
                            Collectors.toMap(FacetFieldEntry::getValue, facetValue -> CheckedCount.builder().count(facetValue.getValueCount()).checked(checkedValues.contains(facetValue.getValue())).build(),
                                    (u, v) -> u,
                                    LinkedHashMap::new
                            )
                    );
        } else {
            return facetValues.stream()
                    .collect(
                            Collectors.toMap(FacetFieldEntry::getValue, facetValue -> CheckedCount.builder().count(facetValue.getValueCount()).build(),
                                    (u, v) -> u,
                                    LinkedHashMap::new
                            )
                    );
        }
    }

    public PaginatedSearchConcepts searchConcepts(String q, boolean advanced, List<String> types,
                                                  @NotNull Map<String, List<String>> filterParams,
                                                  PageCoords pageCoords) throws IllegalFilterException {

        log.debug("filterParams " + filterParams.toString());

        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()); // SOLR counts from page 0
        SearchQueryCriteria queryCriteria = new ConceptSearchQueryPhrase(q, advanced);

        List<SearchFilterCriteria> filterCriteria = new ArrayList<>();
        filterCriteria.add(makePropertyTypeCriteria(types));
        filterCriteria.addAll(makeFiltersCriteria(filterParams, IndexType.CONCEPTS));

        FacetPage<IndexConcept> facetPage = searchConceptRepository.findByQueryAndFilters(queryCriteria, filterCriteria, pageable);

        Map<String, CountedPropertyType> typeFacet = gatherTypeFacet(facetPage, types);
        Map<String, Map<String, CheckedCount>> facets = gatherSearchConceptFacets(facetPage, filterParams);

        return PaginatedSearchConcepts.builder()
                .q(q).concepts(facetPage.get().map(SearchConverter::convertIndexConcept).collect(Collectors.toList()))
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(facetPage.getTotalPages())
                .types(typeFacet)
                .facets(facets)
                .build();

    }

    private Map<String, CountedPropertyType> gatherTypeFacet(FacetPage<IndexConcept> facetPage, List<String> types) {
        Map<String, PropertyType> propertyTypes = propertyTypeService.getAllPropertyTypes();
        List<CountedPropertyType> countedPropertyTypes = facetPage.getFacetFields().stream()
                .filter(field -> field.getName().equals(IndexConcept.TYPES_FIELD))
                .map(facetPage::getFacetResultPage)
                .flatMap(facetFieldEntries -> facetFieldEntries.getContent().stream())
                .map(entry -> SearchConverter.convertPropertyTypeFacet(entry, types, propertyTypes))
                .collect(Collectors.toList());

        return countedPropertyTypes.stream()
                .collect(Collectors.toMap(CountedPropertyType::getCode, countedPropertyType -> countedPropertyType,
                        (u, v) -> u,
                        LinkedHashMap::new));
    }

    private Map<String, Map<String, CheckedCount>> gatherSearchConceptFacets(FacetPage<IndexConcept> facetPage, Map<String, List<String>> filterParams) {
        return facetPage.getFacetFields().stream()
                .filter(field -> !field.getName().equals(IndexConcept.TYPES_FIELD))
                .map(field -> Pair.create(
                        field.getName().replace('_', '-'),
                        createFacetDetails(
                                facetPage.getFacetResultPage(field.getName()).getContent(),
                                filterParams.get(field.getName().replace('_', '-'))
                        )
                ))
                .collect(Collectors.toMap(
                        Pair::getKey, Pair::getValue,
                        (u, v) -> u,
                        LinkedHashMap::new
                ));
    }

    private SearchFilterCriteria makeCategoryCriteria(List<ItemCategory> categories) {
        return createFilterCriteria(SearchFilter.CATEGORY, ItemCategoryConverter.convertCategories(categories));
    }

    private SearchFilterCriteria makePropertyTypeCriteria(List<String> types) {
        return createFilterCriteria(SearchFilter.PROPERTY_TYPE, types);
    }

    private List<SearchFilterCriteria> makeFiltersCriteria(@NotNull Map<String, List<String>> filterParams, IndexType indexType)
            throws IllegalFilterException {
        Map<String, SearchFilter> filters = filterParams.keySet().stream()
                // https://bugs.openjdk.java.net/browse/JDK-8148463
                //        .collect(Collectors.toMap(filterName -> filterName, filterName -> SearchFilter.ofKey(filterName, indexType)));
                .collect(HashMap::new, (map, filterName) -> map.put(filterName, SearchFilter.ofKey(filterName, indexType)), HashMap::putAll);

        for (Map.Entry<String, SearchFilter> entry : filters.entrySet()) {
            if (entry.getValue() == null) {
                throw new IllegalFilterException(entry.getKey());
            }
        }

        return filterParams.keySet().stream()
                .map(filterName -> createFilterCriteria(filters.get(filterName), filterParams.get(filterName)))
                .collect(Collectors.toList());
    }

    private SearchFilterCriteria createFilterCriteria(SearchFilter filter, List<String> values) {
        switch (filter.getType()) {
            case VALUES_SELECTION_FILTER:
                if (values == null) {
                    values = Collections.emptyList();
                }
                return new SearchFilterValuesSelection(filter, values);
            // TODO other types of filter types
            default:
                throw new RuntimeException();   // impossible to happen
        }
    }

    private List<SearchExpressionCriteria> makeExpressionCriteria(@NotNull Map<String, String> expressionParams) {
        return expressionParams.keySet().stream()
                .map(code -> createExpressionCriteria(code, expressionParams.get(code)))
                .collect(Collectors.toList());
    }

    private SearchExpressionCriteria createExpressionCriteria(String code, String expression) {

        if (expression.contains("/")) expression = ClientUtils.escapeQueryChars(expression);

        PropertyType propertyType = propertyTypeService.loadPropertyTypeOrNull(code);
        if (propertyType != null) {
            return new SearchExpressionDynamicFieldCriteria(code, expression, propertyType.getType());
        } else {
            return new SearchExpressionCriteria(code, expression);
        }
    }


    public PaginatedSearchActor searchActors(String q, boolean advanced, @NotNull Map<String, String> expressionParams, PageCoords pageCoords) {

        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());// SOLR counts from page 0

        SearchQueryCriteria queryCriteria = new ActorSearchQueryPhrase(q, advanced);

        List<SearchExpressionCriteria> expressionCriteria = makeExpressionCriteria(expressionParams);

        FacetPage<IndexActor> facetPage = searchActorRepository.findByQueryAndFilters(queryCriteria, expressionCriteria, pageable);

        PaginatedSearchActor result = PaginatedSearchActor.builder()
                .q(q)
                .actors(facetPage.get().map(SearchConverter::convertIndexActor).collect(Collectors.toList()))
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(facetPage.getTotalPages())
                .build();


        // TODO index affiliations directly in SOLR in nested docs (?) -
        // TODO in a similar way add external identifiers to the result
        for (SearchActor searchActor : result.getActors()) {
            Actor actor = actorService.loadActor(searchActor.getId());
            searchActor.setExternalIds(ActorExternalIdMapper.INSTANCE.toDto(actor.getExternalIds()));
            searchActor.setAffiliations(ActorMapper.INSTANCE.toDto(actor.getAffiliations()));

            if (LoggedInUserHolder.getLoggedInUser() == null || !LoggedInUserHolder.getLoggedInUser().isModerator())
                searchActor.getAffiliations().forEach(affiliation -> affiliation.setEmail(null));

        }
        return result;
    }


    public SuggestedSearchPhrases autocompleteItemsSearch(String searchPhrase, ItemCategory context) {
        if (StringUtils.isBlank(searchPhrase))
            throw new IllegalArgumentException("Search phrase must not be empty nor contain only whitespace");

        List<SuggestedObject> suggestions = searchItemRepository.autocompleteSearchQuery(searchPhrase, context);

        return SuggestedSearchPhrases.builder()
                .phrase(searchPhrase)
                .suggestions(suggestions)
                .build();
    }

}
