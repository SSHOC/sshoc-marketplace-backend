package eu.sshopencloud.marketplace.model.actors;

import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Entity
@Table(name = "actor_roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorRole implements OrderableEntity<String> {

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
