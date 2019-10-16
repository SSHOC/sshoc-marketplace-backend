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
    @JoinColumn(name="subject_id", insertable = false, updatable = false)
    private Long subjectId;

    @Id
    @JoinColumn(name="object_id", insertable = false, updatable = false)
    private Long objectId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private ConceptRelation relation;

}
