package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
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

    public ItemMedia(Item item, UUID mediaId, String caption) {
        this.id = null;
        this.item = item;
        this.mediaId = mediaId;
        this.caption = caption;
        this.itemMediaThumbnail = ItemMediaType.MEDIA;
    }

    public ItemMedia(Item item, UUID mediaId, String caption, ItemMediaType itemThumbnail) {
        this(item, mediaId, caption);
        this.itemMediaThumbnail = itemThumbnail;
    }
}
