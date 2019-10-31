package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.dto.items.ItemRelationId;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemRelationConverter {

    public ItemRelation convert(ItemRelationId itemRelation) {
        ItemRelation result = new ItemRelation();
        result.setCode(itemRelation.getCode());
        return result;
    }

}
