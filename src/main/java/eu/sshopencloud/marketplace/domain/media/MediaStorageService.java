package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDownload;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import eu.sshopencloud.marketplace.domain.media.dto.MediaUploadInfo;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.UUID;

public interface MediaStorageService {

    MediaDownload getMediaForDownload(UUID mediaId);
    MediaDownload getThumbnailForDownload(UUID mediaId);

    boolean ensureMediaAvailable(UUID mediaId);

    MediaDetails saveCompleteMedia(Resource mediaFile, Optional<MediaType> mimeType);
    MediaUploadInfo saveMediaChunk(Optional<UUID> mediaId, Resource mediaChunk, int chunkNo, Optional<MediaType> mimeType);
    MediaDetails completeMediaUpload(UUID mediaId);

    MediaDetails importMedia(MediaLocation mediaLocation);

    MediaDetails linkToMedia(UUID mediaId) throws MediaNotAvailableException;
    MediaDetails removeMediaLink(UUID mediaId);
}
