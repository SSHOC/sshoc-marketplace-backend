package eu.sshopencloud.marketplace.model.actors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "actor_external_sources")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ActorSource {

    @Id
    private String code;

    @Column(nullable = false)
    private String label;

    private int ord;
}
