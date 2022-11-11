package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
public class PropertyDto {

    private PropertyTypeDto type;

    @Nullable
    private String value;

    private ConceptBasicDto concept;

}
