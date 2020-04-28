package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "concept_relations")
@Data
@NoArgsConstructor
public class ConceptRelation {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH }, orphanRemoval = true)
    @JoinColumn(foreignKey = @ForeignKey(name="concept_relation_inverse_of_code_fk"))
    private ConceptRelation inverseOf;

}
