package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.UUID;


@Entity
@Table(name = "media_data")
@DynamicUpdate
@Data
@NoArgsConstructor
class MediaData {

    @Id
    private UUID id;

    @Column(name = "media_category", nullable = false)
    private MediaCategory category;

    private Path filePath;

    private String originalFilename;

    private String mimeType;

    private String sourceUrl;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    private MediaData thumbnail;

    @UpdateTimestamp
    private ZonedDateTime touchTimestamp;

    private long linkCount;


    public MediaData(UUID id, MediaCategory category, Path filePath, String originalFilename, String mimeType) {
        this.id = id;
        this.category = category;
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.thumbnail = null;
        this.linkCount = 0;
    }

    public boolean isTemporary() {
        return (linkCount == 0);
    }

    public boolean isStoredLocally() {
        return (filePath != null);
    }

    public boolean hasThumbnail() {
        return (thumbnail != null);
    }
}
