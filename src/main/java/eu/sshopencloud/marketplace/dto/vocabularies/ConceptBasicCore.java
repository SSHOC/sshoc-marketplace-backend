package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ConceptBasicCore {

    private String code;

    private String label;

    private String notation;

    private String definition;

    private String uri;

}
