package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConceptRelatedConceptInline {

    private Long id;

    private VocabularyInline vocabulary;

    private String label;

    private String description;

    private String uri;

    private ConceptRelation relation;

}
