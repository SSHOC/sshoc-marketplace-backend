package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(ConceptRelatedConceptId.class)
@Table(name = "concepts_related_concepts")
@Data
@NoArgsConstructor
public class ConceptRelatedConcept implements Serializable {

    @Id
    @JoinColumn(name="subject_code", insertable = false, updatable = false)
    private String subjectCode;

    @Id
    @JoinColumn(name="subject_vocabulary_code", insertable = false, updatable = false)
    private String subjectVocabularyCode;

    @Id
    @JoinColumn(name="object_code", insertable = false, updatable = false)
    private String objectCode;

    @Id
    @JoinColumn(name="object_vocabulary_code", insertable = false, updatable = false)
    private String objectVocabularyCode;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private ConceptRelation relation;

}
