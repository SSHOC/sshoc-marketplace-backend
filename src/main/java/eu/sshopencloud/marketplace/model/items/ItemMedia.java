package eu.sshopencloud.marketplace.model.items;

import lombok.*;

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

    private boolean itemThumbnail;


    public ItemMedia(Item item, UUID mediaId, String caption) {
        this.id = null;
        this.item = item;
        this.mediaId = mediaId;
        this.caption = caption;
        this.itemThumbnail = false;
    }

    public ItemMedia(Item item, UUID mediaId, String caption, boolean itemThumbnail) {
        this(item, mediaId, caption);
        this.itemThumbnail = itemThumbnail;
    }
}
