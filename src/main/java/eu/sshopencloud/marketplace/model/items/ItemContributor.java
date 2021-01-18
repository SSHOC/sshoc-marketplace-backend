package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "items_contributors")
@Data
@ToString(exclude = "item")
@EqualsAndHashCode(exclude = "item")
@NoArgsConstructor
@AllArgsConstructor
public class ItemContributor implements Serializable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="item_contributor_item_id_fk"))
    private Item item;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="item_contributor_actor_id_fk"))
    private Actor actor;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="item_contributor_actor_role_code_fk"))
    private ActorRole role;

    private Integer ord;


    public ItemContributor(Item item, ItemContributor base) {
        this.item = item;
        this.actor = base.getActor();
        this.role = base.getRole();
        this.ord = base.getOrd();
    }

    public ItemContributor(Item item, Actor actor, ActorRole role) {
        this.item = item;
        this.actor = actor;
        this.role = role;
        this.ord = null;
    }
}
