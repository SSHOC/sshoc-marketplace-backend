package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountedConcept {

    private String code;

    private VocabularyId vocabulary;

    private String label;

    private String notation;

    @JsonIgnore
    private Integer ord;

    private String definition;

    private String uri;

    private long count;

    private boolean checked;

}
