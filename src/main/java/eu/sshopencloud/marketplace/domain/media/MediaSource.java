package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.conf.jpa.UrlConverter;
import eu.sshopencloud.marketplace.domain.common.OrderableEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.net.URL;

@Entity
@Table(name = "media_sources")
@Data
@NoArgsConstructor
class MediaSource implements OrderableEntity<String> {

    @Id
    private String code;

    @Convert(converter = UrlConverter.class)
    @Column(nullable = false)
    private URL serviceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaCategory mediaCategory;

    private int ord;


    public MediaSource(String code, URL serviceUrl, MediaCategory mediaCategory) {
        this.code = code;
        this.serviceUrl = serviceUrl;
        this.mediaCategory = mediaCategory;
    }


    @Override
    public String getId() {
        return code;
    }
}
