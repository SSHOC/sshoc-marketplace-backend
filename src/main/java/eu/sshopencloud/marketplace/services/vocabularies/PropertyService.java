package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.VocabularyInline;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    private final PropertyTypeService propertyTypeService;

    private final ConceptService conceptService;

    public List<Property> getItemProperties(Long itemId) {
        return propertyRepository.findPropertyByItemIdOrderByOrd(itemId);
    }

    public List<Property> validate(ItemCategory category, String prefix, List<PropertyCore> properties, Item item)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException, TooManyObjectTypesException {
        List<Property> result = new ArrayList<Property>();
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                PropertyCore property = properties.get(i);
                result.add(validate(category, prefix + "[" + i + "].", property, item));
            }
        }
        boolean missingObjectType = true;
        for (int i = 0; i < result.size(); i++) {
            Property property = result.get(i);
            if (property.getType().getCode().equals(ItemCategory.OBJECT_TYPE_PROPERTY_TYPE_CODE)) {
                if (!missingObjectType) {
                    throw new TooManyObjectTypesException(category, prefix + "[" + i + "].concept.code", property.getConcept().getCode());
                }
                missingObjectType = false;
            }
        }
        if (missingObjectType) {
            result.add(getDefaultObjectTypeProperty(category, item));
        }
        return result;
    }

    public Property validate(ItemCategory category, String prefix, PropertyCore property, Item item)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException {
        Property result = new Property();
        result.setItem(item);
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
            result.setConcept(conceptService.validate(category,prefix + "concept.", property.getConcept(), propertyType, allowedVocabularies));
        }
        return result;
    }

    private Property getDefaultObjectTypeProperty(ItemCategory category, Item item) {
        Property result = new Property();
        result.setItem(item);
        result.setType(propertyTypeService.getPropertyType(ItemCategory.OBJECT_TYPE_PROPERTY_TYPE_CODE));
        result.setConcept(conceptService.getDefaultObjectTypeConcept(category));
        return result;
    }

}
