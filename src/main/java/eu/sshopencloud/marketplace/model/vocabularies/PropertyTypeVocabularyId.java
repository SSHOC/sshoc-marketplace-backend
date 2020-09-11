package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeVocabularyId implements Serializable {

    private String propertyType;

    private String vocabulary;

}
