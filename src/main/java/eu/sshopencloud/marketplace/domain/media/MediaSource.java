package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "media_sources")
@Data
@NoArgsConstructor
class MediaSource implements OrderableEntity<String> {

    @Id
    private String code;

    @Column(nullable = false)
    private String serviceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaCategory mediaCategory;

    private int ord;


    public MediaSource(String code, String serviceUrl, MediaCategory mediaCategory) {
        this.code = code;
        this.serviceUrl = serviceUrl;
        this.mediaCategory = mediaCategory;
    }


    @Override
    public String getId() {
        return code;
    }
}
