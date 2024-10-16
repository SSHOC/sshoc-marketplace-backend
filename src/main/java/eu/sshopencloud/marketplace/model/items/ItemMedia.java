package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "items_media")
@Data
@EqualsAndHashCode(exclude = "item")
@ToString(exclude = "item")
@NoArgsConstructor
public class ItemMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "items_media_id_gen")
    @SequenceGenerator(name = "items_media_id_gen", sequenceName = "items_media_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Item item;

    private UUID mediaId;

    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemMediaType itemMediaThumbnail;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumns(
            value = { @JoinColumn(name = "code"), @JoinColumn(name = "vocabulary_code") },
            foreignKey = @ForeignKey(name="item_media_concept_fk")
    )
    private Concept concept;

    public ItemMedia(Item item, UUID mediaId, String caption) {
        this.id = null;
        this.item = item;
        this.mediaId = mediaId;
        this.caption = caption;
        this.itemMediaThumbnail = ItemMediaType.MEDIA;
        this.concept = null;
    }

    public ItemMedia(Item item, UUID mediaId, String caption, ItemMediaType itemThumbnail) {
        this(item, mediaId, caption);
        this.itemMediaThumbnail = itemThumbnail;
    }

    public ItemMedia(Item item, UUID mediaId, String caption, ItemMediaType itemThumbnail, Concept concept) {
        this(item, mediaId, caption, itemThumbnail);
        this.concept = concept;
    }

    public ItemMedia(Item item, UUID mediaId, String caption, Concept concept) {
        this(item, mediaId, caption);
        this.concept = concept;
    }
}
