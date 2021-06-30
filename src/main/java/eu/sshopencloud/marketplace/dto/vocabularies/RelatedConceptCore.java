package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelatedConceptCore {

    private String code;

    private VocabularyId vocabulary;

    private String uri;

    private ConceptRelationId relation;

}
