package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "concept_relations")
@Data
@NoArgsConstructor
public class ConceptRelation {

    @Id
    protected String code;

    @Basic
    @Column(nullable = false)
    @JsonIgnore
    protected Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH }, orphanRemoval = true)
    @JsonIgnore
    private ConceptRelation inverseOf;

}
