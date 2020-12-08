package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyFactory {

    private final ConceptFactory conceptFactory;
    private final PropertyTypeFactory propertyTypeFactory;
    private final PropertyTypeService propertyTypeService;


    public List<Property> create(ItemCategory category, List<PropertyCore> propertyCores, Item item, Errors errors, String nestedPath) {
        List<Property> properties = new ArrayList<>();

        if (propertyCores != null) {
            for (int i = 0; i < propertyCores.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                PropertyCore propertyCore = propertyCores.get(i);
                Property property = create(category, propertyCore, item, errors);
                if (property != null) {
                    properties.add(property);
                }
                errors.popNestedPath();
            }
        }

        return properties;
    }

    public Property create(ItemCategory category, PropertyCore propertyCore, Item item, Errors errors) {
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
            // value is mandatory
            if (StringUtils.isBlank(propertyCore.getValue())) {
                errors.rejectValue("value", "field.required", "Property value is required.");
            } else {
                property.setValue(propertyCore.getValue());
            }
        } else {
            // concept is mandatory
            if (propertyCore.getConcept() == null) {
                errors.rejectValue("concept", "field.required", "Property concept is required.");
            } else {
                errors.pushNestedPath("concept");
                Concept concept = conceptFactory.create(category, propertyCore.getConcept(), propertyType, allowedVocabularies, errors);
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
