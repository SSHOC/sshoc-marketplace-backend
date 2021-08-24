package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

@Data
@SuperBuilder
@NoArgsConstructor
public class ConceptBasicCore {
    
    private String code;

    private String label;

    private String notation;

    @Nullable
    private String definition;

    private String uri;

}
