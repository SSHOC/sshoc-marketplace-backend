package eu.sshopencloud.marketplace.conf.startup.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ItemLoader {

    private final ConceptRepository conceptRepository;

    public void completeProperties(Item newItem) {
        if (newItem.getProperties() != null) {
            for (Property property : newItem.getProperties()) {
                property.setItem(newItem);
                if (property.getConcept() != null) {
                    property.setConcept(conceptRepository.findById(ConceptId.builder().code(property.getConcept().getCode()).vocabulary(property.getConcept().getVocabulary().getCode()).build()).get());
                }
            }
        }
    }

    public void completeContributors(Item newItem) {
        if (newItem.getContributors() != null) {
            for (ItemContributor itemContributor : newItem.getContributors()) {
                itemContributor.setItem(newItem);
            }
        }
    }

}
