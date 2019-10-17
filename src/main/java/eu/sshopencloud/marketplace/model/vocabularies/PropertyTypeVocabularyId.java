package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PropertyTypeVocabularyId implements Serializable {

    private String propertyTypeCode;

    private Long vocabularyId;

}
