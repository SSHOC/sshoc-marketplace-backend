package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class PropertyCore {

    private PropertyTypeId type;
    private String value;
    private ConceptId concept;


    public PropertyCore(PropertyTypeId type, String value) {
        this.type = type;
        this.value = value;
    }

    public PropertyCore(PropertyTypeId type, ConceptId concept) {
        this.type = type;
        this.concept = concept;
    }
}
