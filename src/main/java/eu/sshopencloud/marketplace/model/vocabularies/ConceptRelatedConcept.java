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
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumns({
            @JoinColumn(name = "subject_code", insertable = true, updatable = true),
            @JoinColumn(name = "subject_vocabulary_code", insertable = true, updatable = true)
    })
    private Concept subject;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumns({
            @JoinColumn(name = "object_code", insertable = true, updatable = true),
            @JoinColumn(name = "object_vocabulary_code", insertable = true, updatable = true)
    })
    private Concept object;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private ConceptRelation relation;

}
