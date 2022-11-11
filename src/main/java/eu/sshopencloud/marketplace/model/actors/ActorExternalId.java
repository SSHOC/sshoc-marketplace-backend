package eu.sshopencloud.marketplace.model.actors;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;


@Entity
@Table(
        name = "actor_external_ids",
        uniqueConstraints = @UniqueConstraint(columnNames = { "identifier_service_code", "identifier", "actor_id" })
)
@Data
@ToString(exclude = "actor")
@EqualsAndHashCode(exclude = "actor")
@NoArgsConstructor
public class ActorExternalId {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_external_id_gen")
    @SequenceGenerator(name = "actor_external_id_gen", sequenceName = "actor_external_ids_id_seq", allocationSize = 1)
    @Expose
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @Expose
    private ActorSource identifierService;

    @Column(nullable = false, length = 2048)
    @Expose
    private String identifier;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Actor actor;


    public ActorExternalId(ActorSource identifierService, String identifier, Actor actor) {
        this.id = null;
        this.identifierService = identifierService;
        this.identifier = identifier;
        this.actor = actor;
    }
}
