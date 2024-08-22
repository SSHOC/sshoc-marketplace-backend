package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Entity
@Table(name = "draft_related_items")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class DraftRelatedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "draft_relations_id_generator")
    @SequenceGenerator(name = "draft_relations_id_generator", sequenceName = "draft_relations_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private DraftItem subject;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private VersionedItem object;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private ItemRelation relation;


    public DraftRelatedItem(DraftItem subject, VersionedItem object, ItemRelation relation) {
        this.subject = subject;
        this.object = object;
        this.relation = relation;
    }
}
