package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConceptId {

    private String code;

    private VocabularyId vocabulary;

    private String uri;

}
