package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.auth.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name = "draft_items")
@Data
@NoArgsConstructor
public class DraftItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_item_id_generator")
    @SequenceGenerator(name = "draft_item_id_generator", sequenceName = "draft_items_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "draft_item_owner_id_fk"))
    private User owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "draft_item_item_id_fk"))
    private Item item;


    public DraftItem(Item item, User owner) {
        this.item = item;
        this.owner = owner;
    }
}
