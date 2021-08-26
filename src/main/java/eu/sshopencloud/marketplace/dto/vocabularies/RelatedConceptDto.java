package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelatedConceptDto {

    private String code;

    private VocabularyBasicDto vocabulary;

    private String label;

    private String notation;

    private String definition;

    private String uri;

    private ConceptRelationDto relation;

    private boolean candidate;

}
