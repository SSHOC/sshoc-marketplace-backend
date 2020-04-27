package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VocabularyBasicDto {

    private String code;

    private String label;

    private String accessibleAt;

}
