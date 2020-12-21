package eu.sshopencloud.marketplace.model.actors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "actor_roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorRole {

    @Id
    private String code;

    @Basic
    @Column(nullable = false)
    private Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
