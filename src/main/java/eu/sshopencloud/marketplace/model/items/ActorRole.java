package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "actor_roles")
@Data
@NoArgsConstructor
public class ActorRole {

    @Id
    protected String code;

    @Basic
    @Column(nullable = false)
    protected Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
