package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.conf.jpa.FilePathConverter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.UUID;

@Entity
@Table(name = "media_file_uploads")
@Data
@NoArgsConstructor
class MediaFileUpload {

    @Id
    private UUID mediaId;

    @Version
    @Column(name = "optlock")
    private long version;

    @Convert(converter = FilePathConverter.class)
    @Column(nullable = false)
    private Path chunksDirectory;

    @Column(nullable = false)
    private int nextChunkNo;

    @Column(nullable = false)
    private long currentSize;

    @Column(nullable = false)
    private boolean completed;


    public MediaFileUpload(UUID mediaId, Path chunksPath) {
        this.mediaId = mediaId;
        this.version = 0;
        this.chunksDirectory = chunksPath;
        this.nextChunkNo = 0;
        this.currentSize = 0;
        this.completed = false;
    }

    public int chunksNumber() {
        return nextChunkNo;
    }

    public void incrementChunkNo() {
        this.nextChunkNo += 1;
    }

    public int getCurrentChunkNo() {
        return this.nextChunkNo - 1;
    }

    public void incrementSize(long bytes) {
        this.currentSize += bytes;
    }
}
