package eu.sshopencloud.marketplace.services.vocabularies.exception;

public class PropertyTypeAlreadyExistsException extends Exception {

    public PropertyTypeAlreadyExistsException(String propertyTypeCode) {
        super(String.format("Property type with code = '%s' already exists", propertyTypeCode));
    }
}
