package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.model.items.Item;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemConverter {

    public ItemBasicDto convertItem(Item item) {
        ItemBasicDto basicItem = new ItemBasicDto();
        updateDtoWithItemData(item, basicItem);
        return basicItem;
    }

    private static void updateDtoWithItemData(Item item, ItemBasicDto basicItem) {
        basicItem.setId(item.getId());
        basicItem.setPersistentId(item.getPersistentId());
        basicItem.setCategory(item.getCategory());
        basicItem.setLabel(item.getLabel());
        basicItem.setVersion(item.getVersion());
        basicItem.setLastInfoUpdate(item.getLastInfoUpdate());
    }

    private ItemExtBasicDto convertItemToExtBasic(Item item) {
        ItemExtBasicDto extBasicDto = new ItemExtBasicDto();
        updateDtoWithItemData(item, extBasicDto);
        extBasicDto.setStatus(item.getStatus());
        return extBasicDto;
    }

    public List<ItemBasicDto> convertItem(List<Item> items) {
        List<ItemBasicDto> basicDtos = new ArrayList<>();
        items.forEach( item -> basicDtos.add(convertItem(item)));
        return basicDtos;
    }

    public List<ItemExtBasicDto> convertItemsToExtBasic(Page<Item> items) {
        return items.stream().map(ItemConverter::convertItemToExtBasic).collect(Collectors.toList());
    }
}
