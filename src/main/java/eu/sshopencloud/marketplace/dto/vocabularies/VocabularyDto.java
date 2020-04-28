package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class VocabularyDto extends VocabularyBasicDto {

    private String description;

    private List<ConceptDto> concepts;

}
