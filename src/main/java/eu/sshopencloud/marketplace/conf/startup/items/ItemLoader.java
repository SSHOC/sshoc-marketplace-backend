package eu.sshopencloud.marketplace.conf.startup.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ItemLoader {

    private final ConceptRepository conceptRepository;

    private final SourceRepository sourceRepository;


    public void completeItemRelations(Item newItem) {
        completeProperties(newItem);
        completeContributors(newItem);
        completeSource(newItem);
    }

    private void completeProperties(Item newItem) {
        if (newItem.getProperties() != null) {
            for (Property property : newItem.getProperties()) {
                if (property.getConcept() != null) {
                    property.setConcept(conceptRepository.findById(ConceptId.builder().code(property.getConcept().getCode()).vocabulary(property.getConcept().getVocabulary().getCode()).build()).get());
                }
            }
        }
    }

    private void completeContributors(Item newItem) {
        if (newItem.getContributors() != null) {
            for (ItemContributor itemContributor : newItem.getContributors()) {
                itemContributor.setItem(newItem);
            }
        }
    }

    private void completeSource(Item newItem) {
        if (newItem.getSource() != null) {
            newItem.setSource(sourceRepository.findById(newItem.getSource().getId()).get());
        }
    }

}
