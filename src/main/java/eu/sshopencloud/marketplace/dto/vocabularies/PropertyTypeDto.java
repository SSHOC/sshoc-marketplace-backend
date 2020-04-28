package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PropertyTypeDto {

    private String code;

    private String label;

    private List<VocabularyBasicDto> allowedVocabularies;

}
