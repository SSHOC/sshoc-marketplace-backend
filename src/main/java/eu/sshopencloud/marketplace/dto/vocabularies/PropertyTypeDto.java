package eu.sshopencloud.marketplace.dto.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PropertyTypeDto {

    private String code;

    private String label;

    private PropertyTypeClass type;

    private int ord;

    private List<VocabularyBasicDto> allowedVocabularies;

}
