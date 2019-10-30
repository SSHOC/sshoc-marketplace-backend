package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConceptConverter {

    public Concept convertWithoutVocabulary(Concept concept) {
        Concept result = new Concept();
        result.setCode(concept.getCode());
        result.setLabel(concept.getLabel());
        result.setOrd(concept.getOrd());
        result.setDescription(concept.getDescription());
        result.setUri(concept.getUri());
        result.setRelatedConcepts(concept.getRelatedConcepts());
        return result;
    }

}
