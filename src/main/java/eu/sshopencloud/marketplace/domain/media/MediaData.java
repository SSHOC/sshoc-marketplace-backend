package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.conf.jpa.FilePathConverter;
import eu.sshopencloud.marketplace.conf.jpa.MediaTypeConverter;
import eu.sshopencloud.marketplace.conf.jpa.UrlConverter;
import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.MediaType;

import javax.persistence.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "media_data")
@Data
@NoArgsConstructor
class MediaData {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_category", nullable = false)
    private MediaCategory category;

    @Convert(converter = FilePathConverter.class)
    private Path filePath;

    private String originalFilename;

    @Convert(converter = MediaTypeConverter.class)
    private MediaType mimeType;

    @Convert(converter = UrlConverter.class)
    private URL sourceUrl;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    private MediaData thumbnail;

    @UpdateTimestamp
    private LocalDateTime touchTimestamp;

    private long linkCount;


    public MediaData(UUID id, MediaCategory category, Path filePath, String originalFilename, MediaType mimeType) {
        this.id = id;
        this.category = category;
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.linkCount = 0;
        this.thumbnail = null;
    }

    public MediaData(UUID id, MediaCategory category, URL sourceUrl, MediaType mimeType) {
        this.id = id;
        this.category = category;
        this.sourceUrl = sourceUrl;
        this.mimeType = mimeType;
        this.filePath = null;
        this.originalFilename = null;
        this.linkCount = 0;
        this.thumbnail = null;
    }

    public void incrementLinkCount() {
        this.linkCount += 1;
    }

    public void decrementLinkCount() {
        this.linkCount -= 1;
    }

    public void setThumbnail(MediaData thumbnail) {
        if (this.thumbnail != null)
            this.thumbnail.decrementLinkCount();

        this.thumbnail = thumbnail;

        if (thumbnail != null)
            this.thumbnail.incrementLinkCount();
    }

    public boolean thumbnailPossible() {
        // TODO change in case, for example, video thumbnail possibilities
        return (category == MediaCategory.IMAGE);
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

    public boolean isThumbnail() {
        return (category == MediaCategory.THUMBNAIL);
    }
}
