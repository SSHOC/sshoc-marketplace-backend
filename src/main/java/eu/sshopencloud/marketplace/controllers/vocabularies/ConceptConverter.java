package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConceptConverter {

    public Concept convert(ConceptId concept) {
        Concept result = new Concept();
        result.setCode(concept.getCode());
        result.setVocabulary(VocabularyConverter.convert(concept.getVocabulary()));
        return result;
    }

}
