package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemExtBasicConverter {

    public ItemExtBasicDto convertItem(Item item) {
        ItemExtBasicDto historyPosition = new ItemExtBasicDto();
        historyPosition.setId(item.getId());
        historyPosition.setPersistentId(item.getPersistentId());
        historyPosition.setCategory(item.getCategory());
        historyPosition.setLabel(item.getLabel());
        historyPosition.setVersion(item.getVersion());

        historyPosition.setLastInfoUpdate(item.getLastInfoUpdate());
        historyPosition.setStatus(item.getStatus());
        historyPosition.setInformationContributor(UserMapper.INSTANCE.toDto(item.getInformationContributor()));

        return historyPosition;
    }

    public List<ItemExtBasicDto> convertItems(List<Item> items) {
        return items.stream().map(ItemExtBasicConverter::convertItem).collect(Collectors.toList());
    }

}
