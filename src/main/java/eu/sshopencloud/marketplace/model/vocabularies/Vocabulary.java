package eu.sshopencloud.marketplace.model.vocabularies;

import eu.sshopencloud.marketplace.conf.jpa.HashMapConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;
import java.util.Map;


@Entity
@Table(name = "vocabularies")
@Data
@ToString(exclude = "concepts")
@EqualsAndHashCode(exclude = "concepts")
@NoArgsConstructor
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

    @Column(nullable = false)
    private String namespace;

    @OneToMany(mappedBy = "vocabulary", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    @OrderBy("ord")
    private List<Concept> concepts;

}
