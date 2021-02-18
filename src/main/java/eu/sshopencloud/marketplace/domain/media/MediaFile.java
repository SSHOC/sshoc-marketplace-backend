package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.UUID;


@Entity
@Table(name = "media_files")
@DynamicUpdate
@Data
@NoArgsConstructor
class MediaFile {

    @Id
    private UUID id;

    private Path filePath;

    private String sourceUrl;

    private Path thumbnailPath;

    private MediaType type;

    private ZonedDateTime timestamp;

    private long linkCount;


    public MediaFile(Path filePath, Path thumbnailPath, MediaType type) {
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
        this.type = type;
        this.linkCount = 0;
    }

    public boolean isTemporary() {
        return (linkCount == 0);
    }
}
