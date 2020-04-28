package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
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
public class PropertyValidator {

    private final PropertyTypeValidator propertyTypeValidator;

    private final ConceptValidator conceptValidator;

    private final PropertyTypeService propertyTypeService;

    private final ConceptService conceptService;


    public List<Property> validate(ItemCategory category, List<PropertyCore> propertyCores, Item item, Errors errors, String nestedPath) {
        List<Property> properties = new ArrayList<Property>();
        boolean missingObjectType = true;
        if (propertyCores != null) {
            for (int i = 0; i < propertyCores.size(); i++) {
                errors.pushNestedPath(nestedPath + "[" + i + "]");
                PropertyCore propertyCore = propertyCores.get(i);
                Property property = validate(category, propertyCore, item, errors);
                if (property != null) {
                    properties.add(property);
                    if (property.getType().getCode().equals(ItemCategory.OBJECT_TYPE_PROPERTY_TYPE_CODE)) {
                        if (!missingObjectType) {
                            String field = (propertyCore.getConcept().getUri() == null) ? "concept.code" : "concept.uri";
                            errors.rejectValue(field, "field.tooManyObjectTypes", new String[]{category.getValue(), property.getConcept().getCode()},
                                    "More than one object type for category '" + category.getValue() + "'. The '" + property.getConcept().getCode() + "' is superfluous.");
                        }
                        missingObjectType = false;
                    }
                }
                errors.popNestedPath();
            }
        }
        if (missingObjectType) {
            properties.add(getDefaultObjectTypeProperty(category, item));
        }
        if (item.getProperties() != null) {
            item.getProperties().clear();
        }
        return properties;
    }

    private Property getDefaultObjectTypeProperty(ItemCategory category, Item item) {
        Property property = new Property();
        property.setItem(item);
        property.setType(propertyTypeService.getPropertyType(ItemCategory.OBJECT_TYPE_PROPERTY_TYPE_CODE));
        property.setConcept(conceptService.getDefaultObjectTypeConcept(category));
        return property;
    }

    public Property validate(ItemCategory category, PropertyCore propertyCore, Item item, Errors errors) {
        if (propertyCore.getType() == null) {
            errors.rejectValue("type", "field.required", "Property type is required.");
            return null;
        }
        Property property = new Property();
        errors.pushNestedPath("type");
        PropertyType propertyType = propertyTypeValidator.validate(propertyCore.getType(), errors);
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
                Concept concept = conceptValidator.validate(category, propertyCore.getConcept(), propertyType, allowedVocabularies, errors);
                if (concept != null) {
                    property.setConcept(concept);
                }
                errors.popNestedPath();
            }
        }
        if (property.getValue() != null || property.getConcept() != null) {
            property.setItem(item);
            return property;
        } else {
            return null;
        }
    }

}
