package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;


@Entity
@IdClass(ConceptId.class)
@Table(name = "concepts")
@Data
@ToString(exclude = "vocabulary")
@NoArgsConstructor
public class Concept {

    @Id
    private String code;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(name="vocabulary_code", referencedColumnName = "code", insertable = false, updatable = false, foreignKey = @ForeignKey(name="concept_vocabulary_code_fk"))
    private Vocabulary vocabulary;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = false)
    private String notation;

    @Basic
    @Column(nullable = true)
    private Integer ord;

    @Basic
    @Column(nullable = true, length = 4096)
    private String definition;

    @Basic
    @Column(nullable = false, unique = true, length = 2048)
    private String uri;

}
