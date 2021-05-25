package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.mappers.auth.UserMapperImpl;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import lombok.experimental.UtilityClass;

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
        basicItem.setStatus(item.getStatus());
        basicItem.setInformationContributor(UserMapperImpl.INSTANCE.toDto(item.getInformationContributor()));

        return basicItem;
    }
}
