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
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="property_type_code", referencedColumnName = "code", insertable = false, updatable = false, foreignKey = @ForeignKey(name="property_types_vocabularies_property_type_code_fk"))
    private PropertyType propertyType;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="vocabulary_code", referencedColumnName = "code", insertable = false, updatable = false, foreignKey = @ForeignKey(name="property_types_vocabularies_vocabulary_code_fk"))
    private Vocabulary vocabulary;

}