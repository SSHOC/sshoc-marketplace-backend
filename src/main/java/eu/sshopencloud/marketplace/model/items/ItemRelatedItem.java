package eu.sshopencloud.marketplace.model.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(ItemRelatedItemId.class)
@Table(name = "items_related_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRelatedItem implements Serializable {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="subject_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="items_related_items_subject_id_fk"))
    private Item subject;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="object_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="items_related_items_object_id_fk"))
    private Item object;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name="items_related_items_relation_code_fk"))
    private ItemRelation relation;


    public ItemRelatedItem(DraftRelatedItem draftRelation) {
        this.subject = draftRelation.getSubject().getItem();
        this.object = draftRelation.getObject().getCurrentVersion();
        this.relation = draftRelation.getRelation();
    }

    public ItemRelatedItemId getId() {
        return new ItemRelatedItemId(subject.getId(), object.getId());
    }
}
