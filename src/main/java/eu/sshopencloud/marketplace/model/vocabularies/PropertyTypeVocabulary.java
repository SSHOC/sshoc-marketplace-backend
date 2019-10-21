package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PropertyTypeVocabularyId.class)
@Table(name = "property_types_vocabularies")
@Data
@NoArgsConstructor
public class PropertyTypeVocabulary implements Serializable {

    @Id
    @JoinColumn(name="property_type_code", insertable = false, updatable = false)
    private String propertyTypeCode;

    @Id
    @JoinColumn(name="vocabulary_code", insertable = false, updatable = false)
    private String vocabularyCode;

}