package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedConceptCore {

    private String code;

    private VocabularyId vocabulary;

    private String uri;

    private ConceptRelationId relation;

}
