package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;


    public List<Property> getItemProperties(Long itemId) {
        return propertyRepository.findByItemIdOrderByOrd(itemId);
    }

    public void removePropertiesWithConcepts(List<Concept> concepts) {
        propertyRepository.deleteByConceptIn(concepts);
    }
}
