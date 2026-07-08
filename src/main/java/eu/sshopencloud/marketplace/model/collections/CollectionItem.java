package eu.sshopencloud.marketplace.model.collections;

import eu.sshopencloud.marketplace.model.items.Item;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="collection_items")
@Data
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collection_item_generator")
    @SequenceGenerator(name = "collection_item_generator", sequenceName = "collection_item_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Collection collection;

    @ManyToOne
    private Item item;

    private boolean isSuggested;

    private String comment;
}
