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
        basicItem.setCategory(item.getCategory());
        basicItem.setLabel(item.getLabel());
        basicItem.setVersion(item.getVersion());
        return basicItem;
    }

    public RelatedItemDto convertRelatedItemFromSubject(ItemRelatedItem subjectRelatedItem) {
        RelatedItemDto relatedItem = new RelatedItemDto();
        relatedItem.setId(subjectRelatedItem.getObject().getId());
        relatedItem.setPersistentId(subjectRelatedItem.getObject().getPersistentId());
        relatedItem.setRelation(ItemRelationMapper.INSTANCE.toDto(subjectRelatedItem.getRelation()));

        return completeRelatedItem(relatedItem, subjectRelatedItem.getObject());
    }

    public RelatedItemDto convertRelatedItemFromObject(ItemRelatedItem objectRelatedItem) {
        RelatedItemDto relatedItem = new RelatedItemDto();
        relatedItem.setId(objectRelatedItem.getSubject().getId());
        relatedItem.setPersistentId(objectRelatedItem.getSubject().getPersistentId());
        relatedItem.setRelation(ItemRelationMapper.INSTANCE.toDto(objectRelatedItem.getRelation().getInverseOf()));

        return completeRelatedItem(relatedItem, objectRelatedItem.getSubject());
    }

    private RelatedItemDto completeRelatedItem(RelatedItemDto relatedItem, Item item) {
        relatedItem.setCategory(item.getCategory());
        relatedItem.setLabel(item.getLabel());
        relatedItem.setDescription(item.getDescription());
        return relatedItem;
    }

}
