package eu.sshopencloud.marketplace.controllers.vocabularies.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConceptId {

    private String code;

    private VocabularyId vocabulary;

}
