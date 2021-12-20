package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.CollectionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyFactory {

    private final ConceptPropertyFactory conceptPropertyFactory;
    private final PropertyTypeFactory propertyTypeFactory;
    private final PropertyTypeService propertyTypeService;

    private final PropertyValueValidator propertyValueValidator;


    public List<Property> create(List<PropertyCore> propertyCores, Item item, Errors errors, String nestedPath) {
        List<Property> properties = new ArrayList<>();

        if (propertyCores != null && !CollectionUtils.isAllNulls(propertyCores)) {
            for (int i = 0; i < propertyCores.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                PropertyCore propertyCore = propertyCores.get(i);
                Property property = create(propertyCore, item, errors);
                if (property != null) {
                    properties.add(property);
                }
                errors.popNestedPath();
            }
        }

        return properties;
    }

    public Property create(PropertyCore propertyCore, Item item, Errors errors) {
        if (propertyCore.getType() == null) {
            errors.rejectValue("type", "field.required", "Property type is required.");
            return null;
        }
        Property property = new Property();
        errors.pushNestedPath("type");
        PropertyType propertyType = propertyTypeFactory.create(propertyCore.getType(), errors);
        if (propertyType != null) {
            property.setType(propertyType);
        }
        errors.popNestedPath();
        if (propertyType == null) {
            return null;
        }

        List<Vocabulary> allowedVocabularies = propertyTypeService.getAllowedVocabulariesForPropertyType(propertyType);
        if (allowedVocabularies.isEmpty()) {
            String propertyValue = propertyCore.getValue();
            // value is mandatory
            if (propertyValueValidator.validate(propertyValue, propertyType, errors))
                property.setValue(propertyCore.getValue());
        } else {
            // concept is mandatory
            if (propertyCore.getConcept() == null) {
                errors.rejectValue("concept", "field.required", "Property concept is required.");
            } else {
                errors.pushNestedPath("concept");
                Concept concept = conceptPropertyFactory.create(propertyCore.getConcept(), propertyType, allowedVocabularies, errors);
                if (concept != null) {
                    property.setConcept(concept);
                }
                errors.popNestedPath();
            }
        }
        if (property.getValue() != null || property.getConcept() != null)
            return property;

        return null;
    }

}
