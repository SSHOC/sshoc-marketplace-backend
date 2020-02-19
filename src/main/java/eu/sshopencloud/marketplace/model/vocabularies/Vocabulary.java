package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "vocabularies")
@Data
@NoArgsConstructor
public class Vocabulary {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = false, length = 4096)
    private String description;

    @Basic
    @Column(nullable = true)
    private String accessibleAt;

    @Transient
    private List<Concept> concepts;

}
