package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.services.common.OrderableEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


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

    @Override
    public String getId() {
        return code;
    }
}
