package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PropertyConverter {

    public Property convert(PropertyCore property) {
        Property result = new Property();
        result.setType(PropertyTypeConverter.convert(property.getType()));
        result.setValue(property.getValue());
        result.setConcept(ConceptConverter.convert(property.getConcept()));
        return result;
    }

    public List<Property> convert(List<PropertyCore> properties) {
        List<Property> result = new ArrayList<Property>();
        if (properties != null) {
            for (PropertyCore property : properties) {
                result.add(convert(property));
            }
        }
        return result;
    }

}
