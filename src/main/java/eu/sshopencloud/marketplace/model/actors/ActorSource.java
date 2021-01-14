package eu.sshopencloud.marketplace.model.actors;

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
public class ActorSource {

    @Id
    private String code;

    @Column(nullable = false)
    private String label;

    private int ord;
}