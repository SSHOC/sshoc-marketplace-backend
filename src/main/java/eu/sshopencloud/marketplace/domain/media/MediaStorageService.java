package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSource;
import org.springframework.core.io.Resource;

import java.util.UUID;

public interface MediaStorageService {

    MediaInfo saveCompleteMedia(Resource mediaFile);
    MediaInfo saveMediaChunk(UUID mediaId, Resource mediaChunk, int chunkNo);
    MediaInfo completeMediaUpload(UUID mediaId);

    MediaInfo importMedia(MediaSource mediaSource);

    MediaInfo addMediaLink(UUID mediaId);
    MediaInfo removeMediaLink(UUID mediaId);
}
