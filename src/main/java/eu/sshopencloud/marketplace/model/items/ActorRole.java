package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    protected Integer ord;

    @Basic
    @Column(nullable = false)
    private String label;

}
