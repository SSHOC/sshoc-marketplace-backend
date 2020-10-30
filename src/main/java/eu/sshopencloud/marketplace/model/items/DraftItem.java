package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.services.items.exception.ItemsRelationAlreadyExistsException;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


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

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DraftRelatedItem> relations;


    public DraftItem(Item item, User owner) {
        this.item = item;
        this.owner = owner;
        this.relations = new HashSet<>();
    }


    public DraftRelatedItem addRelation(VersionedItem targetObject, ItemRelation relation)
            throws ItemsRelationAlreadyExistsException {

        ensureRelationDoesNotExist(targetObject);

        DraftRelatedItem draftRelation = new DraftRelatedItem(this, targetObject, relation);
        relations.add(draftRelation);

        return draftRelation;
    }

    public void removeRelation(String targetObjectId) {
        relations.removeIf(rel -> rel.getObject().getPersistentId().equals(targetObjectId));
    }

    private void ensureRelationDoesNotExist(VersionedItem object) throws ItemsRelationAlreadyExistsException {
        String persistentId = object.getPersistentId();
        Optional<DraftRelatedItem> relation = relations.stream()
                .filter(rel -> rel.getObject().getPersistentId().equals(persistentId))
                .findAny();

        if (relation.isPresent()) {
            DraftRelatedItem draftRelation = relation.get();
            ItemRelatedItem itemRelation = new ItemRelatedItem(draftRelation);

            throw new ItemsRelationAlreadyExistsException(itemRelation);
        }
    }
}
