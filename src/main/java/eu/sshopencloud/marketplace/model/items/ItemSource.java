package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import lombok.*;

import jakarta.persistence.*;


@Entity
@Table(name = "item_external_sources")
@Data
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSource implements OrderableEntity<String> {

    @Id
    private String code;

    @Column(nullable = false)
    private String label;

    private int ord;

    @Basic
    @Column(nullable = true)
    private String urlTemplate;

    @Override
    public String getId() {
        return code;
    }
}
