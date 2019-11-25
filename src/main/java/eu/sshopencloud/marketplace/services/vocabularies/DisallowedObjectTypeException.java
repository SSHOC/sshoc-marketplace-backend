package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.items.ItemCategory;

public class DisallowedObjectTypeException extends Exception {

    public DisallowedObjectTypeException(ItemCategory category, String path, String value) {
        super("Incorrect object type '" + value + "' for '" + path + "'! The object type '" + value + "' is outside the set of values for the '" + category.getValue() + "' category.");
    }

}
