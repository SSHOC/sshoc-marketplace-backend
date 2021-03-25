package eu.sshopencloud.marketplace.domain.media;

import lombok.experimental.UtilityClass;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;


@UtilityClass
public class MediaTestUtils {

    public boolean isMediaTemporary(TestEntityManager entityManager, UUID mediaId) {
        MediaData mediaData = entityManager.find(MediaData.class, mediaId);
        return mediaData.isTemporary();
    }
}
