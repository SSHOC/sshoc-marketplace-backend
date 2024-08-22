package eu.sshopencloud.marketplace.model.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import jakarta.persistence.*;

@Entity
@Table(name = "property_types")
@Data
@EqualsAndHashCode
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

    @Nullable
    private String groupName;

    @Column(nullable = false)
    private boolean hidden;

    @Column(nullable = false)
    private int ord;
}
