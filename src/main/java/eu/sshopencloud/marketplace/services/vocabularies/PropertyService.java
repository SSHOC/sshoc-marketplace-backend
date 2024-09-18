package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ItemRepository itemRepository;


    public List<Property> getItemProperties(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Item with given id = %d not found", itemId)));

        return item.getProperties();
    }


    public boolean existPropertiesOfType(@NonNull PropertyType type) {
        return propertyRepository.existsByType(type);
    }


    public void removePropertiesOfType(@NonNull PropertyType type) {
        propertyRepository.deletePropertiesOfType(type);
    }


    public boolean existPropertiesFromVocabulary(String vocabularyCode) {
        return propertyRepository.existsByConceptVocabularyCode(vocabularyCode);
    }


    public void removePropertiesWithConcepts(List<Concept> concepts) {
        List<String> conceptCodes = concepts.stream().map(Concept::getCode).collect(Collectors.toList());
        propertyRepository.deletePropertiesWithConcepts(conceptCodes);
    }


    public boolean existPropertiesWithConcepts(List<Concept> concepts) {
        List<String> conceptCodes = concepts.stream().map(Concept::getCode).collect(Collectors.toList());
        return propertyRepository.existWithConcepts(conceptCodes);
    }


    public void replaceConceptInProperties(Concept concept, Concept mergeConcept) {
        List<Property> properties = propertyRepository.findPropertyByConceptCode(mergeConcept.getCode());
        properties.forEach(property -> property.setConcept(concept));
        propertyRepository.saveAll( properties);
    }
}
