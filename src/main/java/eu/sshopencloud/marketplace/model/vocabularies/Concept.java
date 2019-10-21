package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@IdClass(ConceptId.class)
@Table(name = "concepts")
@Data
@NoArgsConstructor
public class Concept {

    @Id
    private String code;

    @Id
    private String vocabularyCode;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = true)
    @JsonIgnore
    protected Integer ord;

    @Basic
    @Column(nullable = true, length = 4096)
    private String description;

    @Basic
    @Column(nullable = false)
    private String uri;

    /* This field will be handled in a separate manner because in this list should be related concepts considering all relations and inverses of relations and because of cyclical dependencies */
    @Transient
    private List<ConceptRelatedConceptInline> relatedConcepts;

}
