package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
class MediaStorageServiceImpl implements MediaStorageService {

    private final MediaFileStorage mediaFileStorage;
    private final MediaThumbnailService mediaThumbnailService;
    private final MediaCategoryResolver mediaCategoryResolver;
    private final MediaDataRepository mediaDataRepository;
    private final int maxFilenameLength;


    public MediaStorageServiceImpl(MediaFileStorage mediaFileStorage,
                                   MediaThumbnailService mediaThumbnailService,
                                   MediaCategoryResolver mediaCategoryResolver,
                                   MediaDataRepository mediaDataRepository,
                                   @Value("${marketplace.media.maxFilenameLength}") int maxFilenameLength) {

        this.mediaFileStorage = mediaFileStorage;
        this.mediaThumbnailService = mediaThumbnailService;
        this.mediaCategoryResolver = mediaCategoryResolver;
        this.mediaDataRepository = mediaDataRepository;
        this.maxFilenameLength = maxFilenameLength;
    }


    @Override
    public MediaInfo saveCompleteMedia(Resource mediaFile, Optional<MediaType> mimeType) {
        try {
            UUID mediaId = resolveNewMediaId();
            MediaFileInfo fileInfo = mediaFileStorage.storeMediaFile(mediaId, mediaFile);

            String mediaFilename = extractFilename(mediaFile);
            MediaCategory mediaCategory = mediaCategoryResolver.resolve(mediaFile, mimeType, mediaFilename);
            String mediaMimeType = mimeType.map(MediaType::toString).orElse(null);

            MediaData mediaData = new MediaData(mediaId, mediaCategory, fileInfo.getMediaFilePath(), mediaFilename, mediaMimeType);
            mediaData = mediaDataRepository.save(mediaData);

            return toMediaInfo(mediaData);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to save media file");
        }
    }

    private String extractFilename(Resource mediaFile) {
        try {
            String filename = mediaFile.getFile().getName();

            if (StringUtils.isBlank(filename))
                throw new IllegalArgumentException("Filename not present");

            if (filename.length() > maxFilenameLength)
                throw new IllegalArgumentException("Filename too long");

            return filename;
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Failed to extract filename");
        }
    }

    @Override
    public MediaInfo saveMediaChunk(Optional<UUID> mediaId, Resource mediaChunk, int chunkNo) {
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
        MediaData mediaData = changeMediaLinkCount(mediaId, 1);
        return toMediaInfo(mediaData);
    }

    @Override
    public MediaInfo removeMediaLink(UUID mediaId) {
        MediaData mediaData = changeMediaLinkCount(mediaId, -1);
        return toMediaInfo(mediaData);
    }

    private MediaData changeMediaLinkCount(UUID mediaId, long amount) {
        MediaData mediaData = loadMediaFile(mediaId);
        mediaData.setLinkCount(mediaData.getLinkCount() + amount);

        return mediaData;
    }

    private MediaData loadMediaFile(UUID mediaId) {
        return mediaDataRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Media with id %s not found", mediaId)));
    }

    private UUID resolveNewMediaId() {
        return UUID.randomUUID();
    }

    private MediaInfo toMediaInfo(MediaData mediaData) {
        boolean hasThumbnail = (mediaData.getThumbnail() != null);

        return MediaInfo.builder()
                .mediaId(mediaData.getId())
                .category(mediaData.getCategory())
                .filename(mediaData.getOriginalFilename())
                .mimeType(mediaData.getMimeType())
                .hasThumbnail(hasThumbnail)
                .build();
    }
}
