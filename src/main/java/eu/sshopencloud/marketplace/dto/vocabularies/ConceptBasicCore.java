package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConceptBasicCore {

    private String code;

    private String label;

    private String notation;

    private String definition;

    private String uri;


}
