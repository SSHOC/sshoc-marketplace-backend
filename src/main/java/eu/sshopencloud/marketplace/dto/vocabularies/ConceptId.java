package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConceptId {

    private String code;

    private VocabularyId vocabulary;

}
