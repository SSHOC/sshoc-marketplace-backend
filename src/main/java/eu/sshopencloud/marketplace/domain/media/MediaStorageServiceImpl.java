package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
class MediaStorageServiceImpl implements MediaStorageService {

    private final MediaFileStorage mediaFileStorage;
    private final MediaFileRepository mediaFileRepository;


    @Override
    public MediaInfo saveCompleteMedia(Resource mediaFile) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public MediaInfo saveMediaChunk(UUID mediaId, Resource mediaChunk, int chunkNo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public MediaInfo completeMediaUpload(UUID mediaId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public MediaInfo importMedia(MediaSource mediaSource) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public MediaInfo addMediaLink(UUID mediaId) {
        MediaFile mediaFile = changeMediaLinkCount(mediaId, 1);
        return MediaInfoMapper.INSTANCE.toDto(mediaFile);
    }

    @Override
    public MediaInfo removeMediaLink(UUID mediaId) {
        MediaFile mediaFile = changeMediaLinkCount(mediaId, -1);
        return MediaInfoMapper.INSTANCE.toDto(mediaFile);
    }

    private MediaFile changeMediaLinkCount(UUID mediaId, long amount) {
        MediaFile mediaFile = loadMediaFile(mediaId);
        mediaFile.setLinkCount(mediaFile.getLinkCount() + amount);

        return mediaFile;
    }

    private MediaFile loadMediaFile(UUID mediaId) {
        return mediaFileRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Media with id %s not found", mediaId)));
    }
}
