package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
public class VocabularyBasicDto {

    private String code;

    private String label;

    @Nullable
    private String accessibleAt;

    private boolean closed;

}
