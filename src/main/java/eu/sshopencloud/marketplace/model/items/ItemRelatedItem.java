package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(ItemRelatedItemId.class)
@Table(name = "items_related_items")
@Data
@NoArgsConstructor
public class ItemRelatedItem implements Serializable {

    @Id
    @JoinColumn(name="subject_id", insertable = false, updatable = false)
    private Long subjectId;

    @Id
    @JoinColumn(name="object_id", insertable = false, updatable = false)
    private Long objectId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = { CascadeType.REFRESH })
    @JoinColumn
    private ItemRelation relation;

}
