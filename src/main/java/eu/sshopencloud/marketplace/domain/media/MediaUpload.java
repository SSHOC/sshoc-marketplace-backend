package eu.sshopencloud.marketplace.domain.media;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "media_uploads")
@Data
@NoArgsConstructor
public class MediaUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "media_upload_id_gen")
    @SequenceGenerator(name = "media_upload_id_gen", sequenceName = "media_upload_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID mediaId;

    private String filename;

    private String mimeType;

    @CreationTimestamp
    private LocalDateTime created;

    @UpdateTimestamp
    private LocalDateTime updated;


    public MediaUpload(UUID mediaId, String filename, String mimeType) {
        this.id = null;
        this.mediaId = mediaId;
        this.filename = filename;
        this.mimeType = mimeType;
    }
}
