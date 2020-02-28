package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.*;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.search.SearchConceptRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilter;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterValuesSelection;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final SearchItemRepository searchItemRepository;

    private final ConceptService conceptService;

    private final ItemContributorService itemContributorService;

    private final PropertyService propertyService;

    private final SearchConceptRepository searchConceptRepository;

    private final PropertyTypeService propertyTypeService;

    public PaginatedSearchItems searchItems(String q, List<ItemCategory> categories, @NotNull Map<String, List<String>> filterParams, List<SearchOrder> order, int page, int perpage)
            throws IllegalFilterException {
        log.debug("filterParams " + filterParams.toString());
        Pageable pageable = PageRequest.of(page - 1, perpage); // SOLR counts from page 0
        if (StringUtils.isBlank(q)) {
            q = "";
        }

        List<SearchFilterCriteria> filterCriteria = new ArrayList<SearchFilterCriteria>();
        filterCriteria.add(makeCategoryCriteria(categories));
        filterCriteria.addAll(makeFiltersCriteria(filterParams, IndexType.ITEMS));

        if (order == null || order.isEmpty()) {
            order = Collections.singletonList(SearchOrder.SCORE);
        }

        FacetPage<IndexItem> facetPage = searchItemRepository.findByQueryAndFilters(q, filterCriteria, order, pageable);

        Map<ItemCategory, Concept> concepts = conceptService.getAllDefaultObjectTypeConcepts();
        List<CountedConcept> countedCategories = facetPage.getFacetFields().stream()
                .filter(field -> field.getName().equals(IndexItem.CATEGORY_FIELD))
                .map(facetPage::getFacetResultPage)
                .flatMap(facetFieldEntries -> facetFieldEntries.getContent().stream())
                .map(entry -> SearchConverter.convertCategoryFacet(entry, categories, concepts))
                .collect(Collectors.toList());
        // when bug in spring-data-solr-4.1.4 is corrected, set facet.mincount and facet.sort to facet parameters and remove manual filling the list of categories
        SearchConverter.fillMissingCategories(countedCategories, categories, concepts);
        countedCategories.sort(new CountedConceptComparator());

        Map<String, Map<String, CheckedCount>> facets = facetPage.getFacetFields().stream()
                .filter(field -> !field.getName().equals(IndexItem.CATEGORY_FIELD))
                .map(field -> Pair.create(
                        field.getName().replace('_', '-'),
                        createFacetDetails(facetPage.getFacetResultPage(field.getName()).getContent(), filterParams.get(field.getName().replace('_', '-')))
                ))
                .collect(Collectors.toMap(
                        Pair::getKey, Pair::getValue,
                        (u, v) -> u,
                        LinkedHashMap::new
                ));

        PaginatedSearchItems result = PaginatedSearchItems.builder().q(q).order(order).items(facetPage.get().map(SearchConverter::convertIndexItem).collect(Collectors.toList()))
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements()).page(page).perpage(perpage).pages(facetPage.getTotalPages())
                .categories(countedCategories.stream()
                        .collect(Collectors.toMap(countedConcept -> ItemCategoryConverter.convertCategory(countedConcept.getCode()), countedConcept -> countedConcept,
                                (u, v) -> u,
                                LinkedHashMap::new)))
                .facets(facets)
                .build();

        // TODO index contributors and properties directly in SOLR in nested docs (?)
        for (SearchItem item : result.getItems()) {
            item.setContributors(itemContributorService.getItemContributors(item.getId()));
            item.setProperties(propertyService.getItemProperties(item.getId()));
        }

        return result;
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

    public PaginatedSearchConcepts searchConcepts(String q, List<String> types, int page, int perpage) {
        Pageable pageable = PageRequest.of(page - 1, perpage); // SOLR counts from page 0
        if (StringUtils.isBlank(q)) {
            q = "";
        }
        List<SearchFilterCriteria> filterCriteria = new ArrayList<SearchFilterCriteria>();
        filterCriteria.add(makePropertyTypeCriteria(types));

        FacetPage<IndexConcept> facetPage = searchConceptRepository.findByQueryAndFilters(q, filterCriteria, pageable);

        Map<String, PropertyType> propertyTypes = propertyTypeService.getAllPropertyTypes();
        List<CountedPropertyType> countedPropertyTypes = facetPage.getFacetFields().stream()
                .filter(field -> field.getName().equals(IndexConcept.TYPES_FIELD))
                .map(facetPage::getFacetResultPage)
                .flatMap(facetFieldEntries -> facetFieldEntries.getContent().stream())
                .map(entry -> SearchConverter.convertPropertyTypeFacet(entry, types, propertyTypes))
                .collect(Collectors.toList());

        PaginatedSearchConcepts result = PaginatedSearchConcepts.builder().q(q).concepts(facetPage.get().map(SearchConverter::convertIndexConcept).collect(Collectors.toList()))
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements()).page(page).perpage(perpage).pages(facetPage.getTotalPages())
                .types(countedPropertyTypes.stream()
                        .collect(Collectors.toMap(CountedPropertyType::getCode, countedPropertyType -> countedPropertyType,
                                (u, v) -> u,
                                LinkedHashMap::new)))
                .build();
        return result;
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

}
