package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;


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

    @Column(nullable = false, length = 4096)
    private String description;

    @Column
    private String accessibleAt;

    @OneToMany(mappedBy = "vocabulary", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    @OrderBy("ord")
    private List<Concept> concepts;
}
