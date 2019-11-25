package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.items.ItemCategory;

public class TooManyObjectTypesException extends Exception {

    public TooManyObjectTypesException(ItemCategory category, String path, String value) {
        super("Object type for the '" + category.getValue() + "'has to have at most one value. Then the value '" + value + "' for '" + path + "' is incorrect!");
    }

}
