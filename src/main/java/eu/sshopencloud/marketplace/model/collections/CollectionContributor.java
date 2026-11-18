package eu.sshopencloud.marketplace.model.collections;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "collections_contributors")
@Data
@ToString(exclude = "collection")
@EqualsAndHashCode(exclude = "collection")
@NoArgsConstructor
@AllArgsConstructor
public class CollectionContributor implements Serializable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="collection_contributor_collection_id_fk"))
    private Collection collection;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="collection_contributor_actor_id_fk"))
    private Actor actor;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="collection_contributor_actor_role_code_fk"))
    private ActorRole role;

    private Integer ord;

    public CollectionContributor(Collection collection, CollectionContributor base) {
        this.collection = collection;
        this.actor = base.getActor();
        this.role = base.getRole();
        this.ord = base.getOrd();
    }

    public CollectionContributor(Collection collection, Actor actor, ActorRole role) {
        this.collection = collection;
        this.actor = actor;
        this.role = role;
        this.ord = null;
    }

    public String getActorRoleLabel() {
        return role.getLabel();
    }

    public long getActorId() {
        return actor.getId();
    }
}
