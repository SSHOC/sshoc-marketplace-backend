package eu.sshopencloud.marketplace.model.vocabularies;

import eu.sshopencloud.marketplace.conf.jpa.HashMapConverter;
import lombok.*;
import org.springframework.lang.Nullable;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "vocabularies")
@Data
@ToString(exclude = "concepts")
@EqualsAndHashCode(exclude = "concepts")
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    @Id
    private String code;

    @Column(nullable = false)
    private String label;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 2048)
    private Map<String, String> labels;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 2048)
    private Map<String, String> titles;

    @Column(nullable = true, length = 4096)
    @Nullable
    private String description;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 16384)
    private Map<String, String> comments;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 16384)
    private Map<String, String> descriptions;

    @Column
    @Nullable
    private String accessibleAt;

    @Column(nullable = false, unique = true)
    private String namespace;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = HashMapConverter.class)
    @Column(nullable = false, length = 16384)
    private Map<String, String> namespaces;

    @Column(nullable = false, unique = true)
    private String scheme;


    @OneToMany(mappedBy = "vocabulary", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    @OrderBy("ord")
    private List<Concept> concepts;

    @Column(nullable = false)
    private boolean closed;

}
