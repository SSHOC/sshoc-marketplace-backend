package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "properties")
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "property_generator")
    @SequenceGenerator(name = "property_generator", sequenceName = "properties_id_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="property_type_code_fk"))
    private PropertyType type;

    @Column(nullable = true, length = 2048)
    private String value;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumns(
            value = { @JoinColumn(name = "code"), @JoinColumn(name = "vocabulary_code") },
            foreignKey = @ForeignKey(name="property_concept_fk")
    )
    private Concept concept;
}
