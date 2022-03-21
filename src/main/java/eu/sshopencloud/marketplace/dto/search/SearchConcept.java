package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeId;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchConcept {

    private String code;

    private VocabularyId vocabulary;

    private String label;

    private String notation;

    private String definition;

    private String uri;

    private List<PropertyTypeId> types;

    private boolean candidate;

}
