package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotEmpty;

@Data
@SuperBuilder
@NoArgsConstructor
public class ConceptBasicCore {

    @NotEmpty(message = "The code of a concept cannot be empty or null!")
    private String code;

    @NotEmpty(message = "The label of a concept cannot be empty or null!")
    private String label;

    private String notation;

    @Nullable
    private String definition;

    private String uri;

}
