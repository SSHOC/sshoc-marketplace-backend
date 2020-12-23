package eu.sshopencloud.marketplace.services.items.exception;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;

public class ItemsRelationAlreadyExistsException extends Exception {

    public ItemsRelationAlreadyExistsException(ItemRelatedItem itemRelatedItem) {
        super("Items relation between " + itemRelatedItem.getSubject().getId() + " and " + itemRelatedItem.getObject().getId() + " already exist: " + itemRelatedItem.getRelation().getCode() + "!");
    }

}
