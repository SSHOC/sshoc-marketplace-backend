package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.VocabularyInline;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyTypeService propertyTypeService;

    private final ConceptService conceptService;

    public List<Property> validate(String prefix, List<PropertyCore> properties) throws DataViolationException, ConceptDisallowedException{
        List<Property> result = new ArrayList<Property>();
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                PropertyCore property = properties.get(i);
                result.add(validate(prefix + "[" + i + "].", property));
            }
        }
        return result;
    }

    public Property validate(String prefix, PropertyCore property) throws DataViolationException, ConceptDisallowedException {
        Property result = new Property();
        if (property.getType() == null) {
            throw new DataViolationException(prefix + "type", "null");
        }
        PropertyType propertyType = propertyTypeService.validate(prefix + "type.", property.getType());
        result.setType(propertyType);
        List<VocabularyInline> allowedVocabularies = propertyTypeService.getAllowedVocabulariesForPropertyType(propertyType);
        if (allowedVocabularies.isEmpty()) {
            // value is mandatory
            if (StringUtils.isBlank(property.getValue())) {
                throw new DataViolationException(prefix + "value", property.getValue());
            }
            result.setValue(property.getValue());
        } else {
            // concept is mandatory
            if (property.getConcept() == null) {
                throw new DataViolationException(prefix + "concept", "null");
            }
            result.setConcept(conceptService.validate(prefix + "concept.", property.getConcept(), propertyType, allowedVocabularies));
        }
        return result;
    }


}
