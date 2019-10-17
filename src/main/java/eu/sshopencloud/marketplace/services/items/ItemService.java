package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final PropertyTypeService propertyTypeService;

    public void fillAllowedVocabulariesForPropertyTypes(Item item) {
        for (Property property: item.getProperties()) {
            PropertyType propertyType = property.getType();
            propertyType.setAllowedVocabularies(propertyTypeService.getAllowedVocabulariesForPropertyType(propertyType));
        }
    }

}
