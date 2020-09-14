package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "property_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyType {

    @Id
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyTypeClass type;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Integer ord;
}
