package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;


public class PropertyTypeClassFormatter extends BaseEnumFormatter<PropertyTypeClass> {

    private static final String PROPERTY_TYPE_CLASS_NAME = "property-type-class";

    @Override
    protected PropertyTypeClass toEnum(String enumValue) {
        return PropertyTypeClass.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return PROPERTY_TYPE_CLASS_NAME;
    }

}
