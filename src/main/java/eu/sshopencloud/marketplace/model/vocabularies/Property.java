package eu.sshopencloud.marketplace.model.vocabularies;

import eu.sshopencloud.marketplace.model.items.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "properties")
@Data
@EqualsAndHashCode(exclude = "item")
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "property_generator")
    @SequenceGenerator(name = "property_generator", sequenceName = "properties_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    private Integer ord;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="property_type_code_fk"))
    private PropertyType type;

    @Basic
    @Column(nullable = true)
    private String value;

    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumns(value = {
            @JoinColumn(name = "code", insertable = true, updatable = true),
            @JoinColumn(name = "vocabulary_code", insertable = true, updatable = true)
    }, foreignKey = @ForeignKey(name="property_concept_fk"))
    private Concept concept;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="property_item_id_fk"))
    private Item item;

}
