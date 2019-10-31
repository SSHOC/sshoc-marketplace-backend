package eu.sshopencloud.marketplace.controllers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VocabularyConverter {

    public Vocabulary convert(VocabularyId vocabulary) {
        Vocabulary result = new Vocabulary();
        result.setCode(vocabulary.getCode());
        return result;
    }

}
