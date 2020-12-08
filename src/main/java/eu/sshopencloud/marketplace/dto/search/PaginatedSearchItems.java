package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonGetter;
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
public class PaginatedSearchItems extends PaginatedResult<SearchItem> {

    private String q;

    private List<SearchOrder> order;

    private List<SearchItem> items;

    private Map<ItemCategory, LabeledCheckedCount> categories;

    private Map<String, Map<String, CheckedCount>> facets;


    @Override
    @JsonGetter("items")
    public List<SearchItem> getResults() {
        return items;
    }
}
