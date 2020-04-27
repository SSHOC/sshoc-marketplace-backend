package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropertyDto {

    private Long id;

    private PropertyTypeDto type;

    private String value;

    private ConceptBasicDto concept;

}
