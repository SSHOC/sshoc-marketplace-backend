package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.dto.search.CountedConcept;
import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.dto.search.SearchItem;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
public class PaginatedSearchItems extends PaginatedResult {

    private String q;

    private List<SearchOrder> order;

    private List<SearchItem> items;

    private Map<ItemCategory, CountedConcept> categories;

    private Map<String, Map<String, Long>> facets;

}
