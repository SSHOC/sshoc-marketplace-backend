package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConceptRelatedConceptInline {

    private String code;

    private VocabularyInline vocabulary;

    private String label;

    private String definition;

    private String uri;

    private ConceptRelation relation;

}
