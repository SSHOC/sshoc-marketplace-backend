package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropertyCore {

    private PropertyTypeId type;

    private String value;

    private ConceptId concept;

}
