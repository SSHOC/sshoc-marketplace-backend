package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "property_types")
@Data
@NoArgsConstructor
public class PropertyType {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(nullable = true) // TODO
    private PropertyTypeClass type;

}
