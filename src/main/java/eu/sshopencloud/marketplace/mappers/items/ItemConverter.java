package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.model.items.Item;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemConverter {

    public ItemBasicDto convertItem(Item item) {
        ItemBasicDto basicItem = new ItemBasicDto();
        basicItem.setId(item.getId());
        basicItem.setPersistentId(item.getPersistentId());
        basicItem.setCategory(item.getCategory());
        basicItem.setLabel(item.getLabel());
        basicItem.setVersion(item.getVersion());
        basicItem.setLastInfoUpdate(item.getLastInfoUpdate());
        return basicItem;
    }

    public List<ItemBasicDto> convertItem(List<Item> items) {
        List<ItemBasicDto> basicDtos = new ArrayList<>();
        items.forEach( item -> {
            ItemBasicDto basicItem = new ItemBasicDto();
            basicItem.setId(item.getId());
            basicItem.setPersistentId(item.getPersistentId());
            basicItem.setCategory(item.getCategory());
            basicItem.setLabel(item.getLabel());
            basicItem.setVersion(item.getVersion());
            basicItem.setLastInfoUpdate(item.getLastInfoUpdate());
            basicDtos.add(basicItem);
        });
        return basicDtos;
    }
}
