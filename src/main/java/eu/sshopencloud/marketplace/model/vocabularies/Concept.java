package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "concepts")
@Data
@NoArgsConstructor
public class Concept {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_generator")
    @SequenceGenerator(name = "concept_generator", sequenceName = "concepts_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = false, length = 4096)
    private String description;

    @Basic
    @Column(nullable = false)
    private String uri;

    /* This field will be handled in a separate manner because in this list should be related concepts considering all relations and inverses of relations and because of cyclical dependencies */
    @Transient
    private List<ConceptRelatedConceptInline> relatedConcepts;

}
