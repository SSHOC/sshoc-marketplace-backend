package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final SearchItemRepository searchItemRepository;

    public PaginatedSearchItems searchItems(String q, List<ItemCategory> categories, List<SearchOrder> order, int page, int perpage) {
        Pageable pageable = PageRequest.of(page, perpage);
        if (StringUtils.isBlank(q)) {
            q = "";
        }
        if (order == null || order.isEmpty()) {
            order = Arrays.asList(new SearchOrder[] { SearchOrder.NAME });
        }

        // TODO apply category filter
        FacetPage<IndexItem> facetPage = searchItemRepository.findByQueryAndCategories(q, ItemCategoryConverter.convertCategories(categories),
                SearchOrderConverter.convertOrder(order), pageable);

        // TODO read category facet
        return PaginatedSearchItems.builder().order(order).items(facetPage.get().map(SearchConverter::convertIndexItem).collect(Collectors.toList()))
                .hits(facetPage.getTotalElements()).count(facetPage.getNumberOfElements()).page(page).perpage(perpage).pages(facetPage.getTotalPages())
                .build();
    }

}
