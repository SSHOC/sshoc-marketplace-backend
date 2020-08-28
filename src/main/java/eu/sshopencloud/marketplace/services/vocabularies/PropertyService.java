package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.vocabularies.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;


    public List<Property> getItemProperties(Long itemId) {
        return propertyRepository.findByItemIdOrderByOrd(itemId);
    }

    public void removePropertiesWithConcepts(List<Concept> concepts) {
        List<String> conceptCodes = concepts.stream().map(Concept::getCode).collect(Collectors.toList());
        propertyRepository.deletePropertiesWithConcepts(conceptCodes);
    }
}
