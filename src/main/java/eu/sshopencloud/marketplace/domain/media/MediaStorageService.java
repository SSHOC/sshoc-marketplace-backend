package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaDownload;
import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSource;
import eu.sshopencloud.marketplace.domain.media.dto.MediaUploadInfo;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.UUID;

public interface MediaStorageService {

    MediaDownload getMediaForDownload(UUID mediaId);
    MediaDownload getThumbnailForDownload(UUID mediaId);

    MediaInfo saveCompleteMedia(Resource mediaFile, Optional<MediaType> mimeType);
    MediaUploadInfo saveMediaChunk(Optional<UUID> mediaId, Resource mediaChunk, int chunkNo, Optional<MediaType> mimeType);
    MediaInfo completeMediaUpload(UUID mediaId);

    MediaInfo importMedia(MediaSource mediaSource);

    MediaInfo addMediaLink(UUID mediaId);
    MediaInfo removeMediaLink(UUID mediaId);
}
