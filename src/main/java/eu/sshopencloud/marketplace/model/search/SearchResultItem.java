package eu.sshopencloud.marketplace.model.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchResultItem {

    private Long id;

    private String name;

    private String description;

    private ItemCategory category;

}
