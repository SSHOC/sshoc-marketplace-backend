package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemConverter {

    public ItemBasicDto convertItem(Item item) {
        ItemBasicDto basicItem = new ItemBasicDto();
        basicItem.setId(item.getId());
        basicItem.setPersistentId(item.getPersistentId());
        basicItem.setCategory(item.getCategory());
        basicItem.setLabel(item.getLabel());
        basicItem.setVersion(item.getVersion());
        return basicItem;
    }
}
