package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@IdClass(ConceptId.class)
@Table(name = "concepts")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Concept {

    @Id
    private String code;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="vocabulary_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Vocabulary vocabulary;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = true)
    @JsonIgnore
    private Integer ord;

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
