package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.CountedConcept;
import eu.sshopencloud.marketplace.dto.search.SearchItem;
import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.services.items.ItemContributorService;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilter;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterValuesSelection;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    public PaginatedSearchItems searchItems(String q, List<ItemCategory> categories, Map<String, List<String>> filterParams, List<SearchOrder> order, int page, int perpage)
            throws IllegalFilterException {
        Pageable pageable = PageRequest.of(page - 1, perpage); // SOLR counts from page 0
        if (StringUtils.isBlank(q)) {
            q = "";
        }
        if (filterParams == null) {
            filterParams = Collections.emptyMap();
        }
        List<SearchFilterCriteria> filterCriteria = new ArrayList<SearchFilterCriteria>();
        filterCriteria.add(makeCategoryCriteria(categories));
        filterCriteria.addAll(makeFiltersCriteria(filterParams));

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
        countedCategories.sort(new CountedConceptComparator());

        PaginatedSearchItems result = PaginatedSearchItems.builder().q(q).order(order).items(facetPage.get().map(SearchConverter::convertIndexItem).collect(Collectors.toList()))
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements()).page(page).perpage(perpage).pages(facetPage.getTotalPages())
                .categories(countedCategories.stream()
                        .collect(Collectors.toMap(countedConcept -> ItemCategoryConverter.convertCategory(countedConcept.getCode()), countedConcept -> countedConcept,
                                (u, v) -> u,
                                LinkedHashMap::new)))
                .build();

        // TODO index contributors directly in SOLR in nested docs
        for (SearchItem item: result.getItems()) {
            item.setContributors(itemContributorService.getItemContributors(item.getId()));
            item.setProperties(propertyService.getItemProperties(item.getId()));
        }

        return result;
    }


    private SearchFilterCriteria makeCategoryCriteria(List<ItemCategory> categories) {
        return createFilterCriteria(SearchFilter.CATEGORY, ItemCategoryConverter.convertCategories(categories));
    }

    private List<SearchFilterCriteria> makeFiltersCriteria(Map<String, List<String>> filterParams)
            throws IllegalFilterException {
        Map<String, SearchFilter> filters = filterParams.keySet().stream()
                .collect(Collectors.toMap(filterName -> filterName, SearchFilter::ofKey));

        for (Map.Entry<String, SearchFilter> entry: filters.entrySet()) {
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
                return new SearchFilterValuesSelection(filter, values);
            // TODO other types of filter types
            default:
                throw new RuntimeException();   // impossible to happen
        }
    }

}
