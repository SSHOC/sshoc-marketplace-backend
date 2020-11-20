package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.actors.ActorRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "items_contributors")
@Data
@ToString(exclude = "item")
@EqualsAndHashCode(exclude = "item")
@NoArgsConstructor
public class ItemContributor implements Serializable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_contributor_item_id_fk"))
    private Item item;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_contributor_actor_id_fk"))
    private Actor actor;

    @Basic
    private Integer ord;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_contributor_actor_role_code_fk"))
    private ActorRole role;

    public ItemContributor(Item item, ItemContributor base) {
        this.item = item;
        this.actor = base.getActor();
        this.ord = base.getOrd();
        this.role = base.getRole();
    }
}
