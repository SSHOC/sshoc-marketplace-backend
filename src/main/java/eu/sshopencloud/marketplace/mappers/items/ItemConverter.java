package eu.sshopencloud.marketplace.mappers.items;

import eu.sshopencloud.marketplace.dto.items.RelatedItemDto;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemConverter {

    public RelatedItemDto convertRelatedItemFromSubject(ItemRelatedItem subjectRelatedItem) {
        RelatedItemDto relatedItem = new RelatedItemDto();
        relatedItem.setId(subjectRelatedItem.getObject().getId());
        relatedItem.setRelation(ItemRelationMapper.INSTANCE.toDto(subjectRelatedItem.getRelation()));
        completeRelatedItem(relatedItem, subjectRelatedItem.getObject());
        return relatedItem;
    }

    public RelatedItemDto convertRelatedItemFromObject(ItemRelatedItem objectRelatedItem) {
        RelatedItemDto relatedItem = new RelatedItemDto();
        relatedItem.setId(objectRelatedItem.getSubject().getId());
        relatedItem.setRelation(ItemRelationMapper.INSTANCE.toDto(objectRelatedItem.getRelation().getInverseOf()));
        completeRelatedItem(relatedItem, objectRelatedItem.getSubject());
        return relatedItem;
    }

    private RelatedItemDto completeRelatedItem(RelatedItemDto relatedItem, Item item) {
        relatedItem.setCategory(item.getCategory());
        relatedItem.setLabel(item.getLabel());
        relatedItem.setDescription(item.getDescription());
        return relatedItem;
    }

}
