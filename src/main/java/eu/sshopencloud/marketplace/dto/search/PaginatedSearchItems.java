package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedSearchItems extends PaginatedResult {

    private String q;

    private List<SearchOrder> order;

    private List<SearchItem> items;

    private Map<ItemCategory, CheckedCount> categories;

    private Map<String, Map<String, CheckedCount>> facets;

}
