package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertyTypeConverter {

    public PropertyType convert(PropertyTypeId propertyType) {
        PropertyType result = new PropertyType();
        result.setCode(propertyType.getCode());
        return result;
    }

}
