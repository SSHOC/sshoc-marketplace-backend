package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.dto.search.SearchItem;
import eu.sshopencloud.marketplace.services.items.ItemCategoryConverter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchConverter {

    public SearchItem convertIndexItem(IndexItem indexItem) {
        return SearchItem.builder()
                .id(indexItem.getId()).name(indexItem.getName()).description(indexItem.getDescription())
                .category(ItemCategoryConverter.convertCategory(indexItem.getCategory()))
                .build();
    }

}
