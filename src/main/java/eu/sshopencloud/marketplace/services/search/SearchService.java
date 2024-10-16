package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.actors.ActorExternalIdMapper;
import eu.sshopencloud.marketplace.mappers.actors.ActorMapper;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
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
                                            List<ItemSearchOrder> order, PageCoords pageCoords) throws IllegalFilterException {

        log.debug("filterParams " + filterParams.toString());
        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()); // SOLR counts from page 0

        SearchQueryCriteria queryCriteria = new ItemSearchQueryPhrase(q, advanced);

        List<SearchFilterCriteria> filterCriteria = new ArrayList<>();
        if (Objects.nonNull(categories) && !categories.isEmpty()) {
            filterCriteria.add(makeCategoryCriteria(categories));
        }
        if (!filterParams.isEmpty()) {
            filterCriteria.addAll(makeFiltersCriteria(filterParams, IndexType.ITEMS));
        }

        List<SearchExpressionCriteria> expressionCriteria = makeExpressionCriteria(expressionParams);

        if (order == null || order.isEmpty()) {
            order = Collections.singletonList(ItemSearchOrder.SCORE);
        }

        User currentUser = LoggedInUserHolder.getLoggedInUser();

        QueryResponse facetPage = searchItemRepository.findByQueryAndFilters(queryCriteria, expressionCriteria,
                currentUser, includeSteps, filterCriteria, order, pageable);

        Map<ItemCategory, LabeledCheckedCount> categoryFacet = gatherCategoryFacet(facetPage, categories);
        Map<String, Map<String, CheckedCount>> facets = gatherSearchItemFacets(facetPage, filterParams);

        PaginatedSearchItems result = PaginatedSearchItems.builder()
                .q(q)
                .order(order)
                .items(
                        facetPage.getBeans(IndexItem.class).stream()
                                .map(SearchConverter::convertIndexItem)
                                .collect(Collectors.toList())
                )
                .hits(facetPage.getResults().getNumFound()).count(facetPage.getResults().size())
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages((int) Math.ceil((double)facetPage.getResults().getNumFound() / (double)pageCoords.getPerpage()))
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

    private Map<ItemCategory, LabeledCheckedCount> gatherCategoryFacet(QueryResponse facetPage, List<ItemCategory> categories) {
        Map<ItemCategory, LabeledCheckedCount> countedCategories = facetPage.getFacetFields().stream()
                .filter(field -> IndexItem.CATEGORY_FIELD.equals(field.getName()))
                .map(FacetField::getValues)
                .flatMap(Collection::stream)
                .collect(
                        Collectors.toMap(
                                facet -> ItemCategoryConverter.convertCategory(facet.getName()),
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

    private Map<String, Map<String, CheckedCount>> gatherSearchItemFacets(QueryResponse facetPage, Map<String, List<String>> filterParams) {
        return facetPage.getFacetFields().stream()
                .filter(field -> !IndexItem.CATEGORY_FIELD.equals(field.getName()))
                .map(field -> createFacetsDetails(field.getName(), facetPage, filterParams))
                .collect(Collectors.toMap(
                        Pair::getKey, Pair::getValue,
                        (u, v) -> u,
                        LinkedHashMap::new
                ));
    }


    private static Pair<String, Map<String, CheckedCount>> createFacetsDetails(String fieldName, QueryResponse facetPage, Map<String, List<String>> filterParams) {
        String facetName;
        if (fieldName.startsWith(SearchExpressionDynamicFieldCriteria.DYNAMIC_FIELD_PREFIX)) {
            facetName = fieldName.replace(SearchExpressionDynamicFieldCriteria.DYNAMIC_FIELD_PREFIX, "");
            facetName = facetName.substring(0, facetName.lastIndexOf("_"));
        } else {
            facetName = fieldName.replace('_', '-');
        }
        return Pair.of(
                facetName,
                createFacetDetails(
                        facetPage.getFacetField(fieldName).getValues(),
                        filterParams.get(facetName)
                ));
    }

    private static Map<String, CheckedCount> createFacetDetails(List<FacetField.Count> facetValues, List<String> checkedValues) {
        if (checkedValues != null) {
            return facetValues.stream().collect(Collectors.toMap(FacetField.Count::getName,
                    facetValue -> CheckedCount.builder().count(facetValue.getCount())
                            .checked(checkedValues.contains(facetValue.getName())).build(), (u, v) -> u,
                    LinkedHashMap::new));
        } else {
            return facetValues.stream().collect(Collectors.toMap(FacetField.Count::getName,
                    facetValue -> CheckedCount.builder().count(facetValue.getCount()).build(), (u, v) -> u,
                    LinkedHashMap::new));
        }
    }

    public PaginatedSearchConcepts searchConcepts(String q, boolean advanced, List<String> types,
                                                  @NotNull Map<String, List<String>> filterParams,
                                                  PageCoords pageCoords, ConceptSearchOrder order) throws IllegalFilterException {

        log.debug("filterParams " + filterParams.toString());

        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()); // SOLR counts from page 0
        SearchQueryCriteria queryCriteria = new ConceptSearchQueryPhrase(q, advanced);

        List<SearchFilterCriteria> filterCriteria = new ArrayList<>();
        if (Objects.nonNull(types) && !types.isEmpty()) {
            filterCriteria.add(makePropertyTypeCriteria(types));
        }
        if (!filterParams.isEmpty()) {
            filterCriteria.addAll(makeFiltersCriteria(filterParams, IndexType.CONCEPTS));
        }

        QueryResponse facetPage = searchConceptRepository.findByQueryAndFilters(queryCriteria, filterCriteria, pageable, order);

        Map<String, CountedPropertyType> typeFacet = gatherTypeFacet(facetPage, types);
        Map<String, Map<String, CheckedCount>> facets = gatherSearchConceptFacets(facetPage, filterParams);

        return PaginatedSearchConcepts.builder()
                .q(q).concepts(facetPage.getBeans(IndexConcept.class).stream().map(SearchConverter::convertIndexConcept).collect(Collectors.toList()))
                .hits(facetPage.getResults().getNumFound()).count(facetPage.getResults().size())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages((int) Math.ceil((double)facetPage.getResults().getNumFound() / (double)pageCoords.getPerpage()))
                .types(typeFacet)
                .facets(facets)
                .build();
    }

    private Map<String, CountedPropertyType> gatherTypeFacet(QueryResponse facetPage, List<String> types) {
        Map<String, PropertyType> propertyTypes = propertyTypeService.getAllPropertyTypes();
        List<CountedPropertyType> countedPropertyTypes = facetPage.getFacetFields().stream()
                .filter(field -> IndexConcept.TYPES_FIELD.equals(field.getName()))
                .map(FacetField::getValues)
                .flatMap(Collection::stream)
                .map(entry -> SearchConverter.convertPropertyTypeFacet(entry, types, propertyTypes))
                .collect(Collectors.toList());

        return countedPropertyTypes.stream()
                .collect(Collectors.toMap(CountedPropertyType::getCode, countedPropertyType -> countedPropertyType,
                        (u, v) -> u,
                        LinkedHashMap::new));
    }

    private Map<String, Map<String, CheckedCount>> gatherSearchConceptFacets(QueryResponse facetPage, Map<String, List<String>> filterParams) {
        return facetPage.getFacetFields().stream()
                .filter(field -> !IndexConcept.TYPES_FIELD.equals(field.getName()))
                .map(field -> Pair.of(
                        field.getName().replace('_', '-'),
                        createFacetDetails(
                                facetPage.getFacetField(field.getName()).getValues(),
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
        if (filter.getType() == FilterType.VALUES_SELECTION_FILTER) {
            if (values == null) {
                values = Collections.emptyList();
            }
            return new SearchFilterValuesSelection(filter, values);
        }
        throw new RuntimeException();   // impossible to happen
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


    public PaginatedSearchActor searchActors(String q, boolean advanced, @NotNull Map<String, String> expressionParams, PageCoords pageCoords,
            ActorSearchOrder order) {

        Pageable pageable = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage());// SOLR counts from page 0

        SearchQueryCriteria queryCriteria = new ActorSearchQueryPhrase(q, advanced);

        List<SearchExpressionCriteria> expressionCriteria = makeExpressionCriteria(expressionParams);

        QueryResponse facetPage = searchActorRepository.findByQueryAndFilters(queryCriteria, expressionCriteria, pageable, order);

        PaginatedSearchActor result = PaginatedSearchActor.builder()
                .q(q)
                .actors(facetPage.getBeans(IndexActor.class).stream().map(SearchConverter::convertIndexActor).collect(Collectors.toList()))
                .hits(facetPage.getResults().getNumFound()).count(facetPage.getResults().size())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages((int) Math.ceil((double)facetPage.getResults().getNumFound() / (double)pageCoords.getPerpage()))
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
