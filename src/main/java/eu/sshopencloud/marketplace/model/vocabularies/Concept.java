package eu.sshopencloud.marketplace.model.vocabularies;

import eu.sshopencloud.marketplace.conf.jpa.HashMapConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Map;


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
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="vocabulary_code", referencedColumnName = "code", insertable = false, updatable = false, foreignKey = @ForeignKey(name="concept_vocabulary_code_fk"))
    private Vocabulary vocabulary;

    @Basic
    @Column(nullable = false)
    private String label;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 2048)
    private Map<String, String> labels;

    @Basic
    @Column(nullable = false)
    private String notation;

    @Basic
    @Column(nullable = true)
    private Integer ord;

    @Basic
    @Column(nullable = true, length = 4096)
    private String definition;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 16384)
    private Map<String, String> definitions;

    @Basic
    @Column(nullable = false, unique = true, length = 2048)
    private String uri;

    @Basic
    @Column(nullable = false)
    private boolean candidate;

}
