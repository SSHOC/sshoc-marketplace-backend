package eu.sshopencloud.marketplace.model.actors;

import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "actor_external_sources")
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ActorSource implements OrderableEntity<String> {

    @Id
    private String code;

    @Column(nullable = false)
    private String label;

    private int ord;


    public ActorSource(String code, String label) {
        this.code = code;
        this.label = label;
        this.ord = 0;
    }


    @Override
    public String getId() {
        return code;
    }
}
