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
    @JoinColumns(value = {
            @JoinColumn(name = "subject_code", insertable = true, updatable = true),
            @JoinColumn(name = "subject_vocabulary_code", insertable = true, updatable = true)
    }, foreignKey = @ForeignKey(name="concepts_related_concepts_subject_fk"))
    private Concept subject;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumns(value = {
            @JoinColumn(name = "object_code", insertable = true, updatable = true),
            @JoinColumn(name = "object_vocabulary_code", insertable = true, updatable = true)
    }, foreignKey = @ForeignKey(name="concepts_related_concepts_object_fk"))
    private Concept object;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="concepts_related_concepts_relation_code_fk"))
    private ConceptRelation relation;

}
