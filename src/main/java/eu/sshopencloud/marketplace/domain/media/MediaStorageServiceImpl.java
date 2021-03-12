package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.MediaExternalClient.MediaMetadata;
import eu.sshopencloud.marketplace.domain.media.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
@Slf4j
class MediaStorageServiceImpl implements MediaStorageService {

    private final MediaFileStorage mediaFileStorage;
    private final MediaThumbnailService mediaThumbnailService;
    private final MediaCategoryResolver mediaCategoryResolver;
    private final MediaExternalClient mediaExternalClient;

    private final MediaDataRepository mediaDataRepository;
    private final MediaUploadRepository mediaUploadRepository;

    private final int maxFilenameLength;


    public MediaStorageServiceImpl(MediaFileStorage mediaFileStorage,
                                   MediaThumbnailService mediaThumbnailService,
                                   MediaCategoryResolver mediaCategoryResolver,
                                   MediaExternalClient mediaExternalClient,
                                   MediaDataRepository mediaDataRepository,
                                   MediaUploadRepository mediaUploadRepository,
                                   @Value("${marketplace.media.files.maxFilenameLength}") int maxFilenameLength) {

        this.mediaFileStorage = mediaFileStorage;
        this.mediaThumbnailService = mediaThumbnailService;
        this.mediaCategoryResolver = mediaCategoryResolver;
        this.mediaExternalClient = mediaExternalClient;
        this.mediaDataRepository = mediaDataRepository;
        this.mediaUploadRepository = mediaUploadRepository;
        this.maxFilenameLength = maxFilenameLength;
    }


    @Override
    public MediaDownload getMediaForDownload(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);

        if (!mediaData.isStoredLocally())
            throw new IllegalArgumentException("Requested media file comes from an external source");

        return retrieveDownloadData(mediaData);
    }

    @Override
    public MediaDownload getThumbnailForDownload(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);

        if (mediaData.getThumbnail() == null)
            throw new EntityNotFoundException(String.format("Thumbnail for media with id %s is not present", mediaId));

        MediaData thumbnail = mediaData.getThumbnail();
        return retrieveDownloadData(thumbnail);
    }

    private MediaDownload retrieveDownloadData(MediaData media) {
        MediaFileHandle mediaHandle = mediaFileStorage.retrieveMediaFile(media.getId());

        return MediaDownload.builder()
                .mediaFile(mediaHandle.getMediaFile())
                .filename(media.getOriginalFilename())
                .mimeType(media.getMimeType())
                .build();
    }

    @Override
    public MediaDetails saveCompleteMedia(Resource mediaFile, Optional<MediaType> mimeType) {
        UUID mediaId = resolveNewMediaId();
        MediaFileInfo fileInfo = mediaFileStorage.storeMediaFile(mediaId, mediaFile);

        String mediaFilename = extractFilename(mediaFile);
        MediaData mediaData = prepareMediaData(fileInfo, mediaFilename, mimeType);

        if (mediaData.thumbnailPossible()) {
            MediaData thumbnailData = prepareThumbnail(mediaData);
            mediaData.setThumbnail(thumbnailData);
        }

        mediaData = mediaDataRepository.save(mediaData);

        return toMediaDetails(mediaData);
    }

    private MediaData prepareThumbnail(MediaData media) {
        Resource thumbnailFile = mediaThumbnailService.generateThumbnail(media.getFilePath());
        UUID thumbnailId = resolveNewMediaId();
        MediaFileInfo thumbnailInfo = mediaFileStorage.storeMediaFile(thumbnailId, thumbnailFile);

        String thumbnailFilename = mediaThumbnailService.getDefaultThumbnailFilename();
        return new MediaData(
                thumbnailId, MediaCategory.THUMBNAIL, thumbnailInfo.getMediaFilePath(), thumbnailFilename, MediaType.IMAGE_JPEG
        );
    }

    private MediaData prepareMediaData(MediaFileInfo mediaFileInfo, String mediaFilename, Optional<MediaType> mimeType) {
        UUID mediaId = mediaFileInfo.getMediaId();
        MediaCategory mediaCategory = mediaCategoryResolver.resolve(mimeType, mediaFilename);
        MediaType mediaMimeType = mimeType.orElse(null);

        return new MediaData(mediaId, mediaCategory, mediaFileInfo.getMediaFilePath(), mediaFilename, mediaMimeType);
    }

    @Override
    public MediaUploadInfo saveMediaChunk(Optional<UUID> mediaId, Resource mediaChunk, int chunkNo, Optional<MediaType> mimeType) {
        Optional<MediaUpload> ongoingMediaUpload = mediaId.flatMap(mediaUploadRepository::findByMediaId);

        if (ongoingMediaUpload.isPresent()) {
            MediaChunkInfo chunkInfo = mediaFileStorage.storeNextMediaChunk(mediaId.get(), mediaChunk, chunkNo);
            MediaUpload mediaUpload = ongoingMediaUpload.get();

            return toMediaUploadInfo(mediaUpload, chunkInfo.getNextChunkNo());
        }
        else {
            if (mediaId.isPresent())
                throw new IllegalArgumentException(String.format("No ongoing media upload with id: %s", mediaId));

            UUID newMediaId = resolveNewMediaId();
            String mediaFilename = extractFilename(mediaChunk);
            String mediaType = mimeType.map(MimeType::toString).orElse(null);

            MediaUpload mediaUpload = new MediaUpload(newMediaId, mediaFilename, mediaType);
            MediaChunkInfo chunkInfo = mediaFileStorage.storeFirstMediaChunk(newMediaId, mediaChunk, chunkNo);

            mediaUpload = mediaUploadRepository.save(mediaUpload);

            return toMediaUploadInfo(mediaUpload, chunkInfo.getNextChunkNo());
        }
    }

    @Override
    public MediaDetails completeMediaUpload(UUID mediaId) {
        MediaUpload mediaUpload = mediaUploadRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media upload with id %s not found"));

        MediaFileInfo mediaFileInfo = mediaFileStorage.completeMediaUpload(mediaId);

        Optional<MediaType> mimeType = Optional.ofNullable(mediaUpload.getMimeType()).map(MediaType::parseMediaType);
        MediaData mediaData = prepareMediaData(mediaFileInfo, mediaUpload.getFilename(), mimeType);

        if (mediaData.thumbnailPossible()) {
            MediaData thumbnailData = prepareThumbnail(mediaData);
            mediaData.setThumbnail(thumbnailData);
        }

        mediaData = mediaDataRepository.save(mediaData);

        return toMediaDetails(mediaData);
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
            throw new IllegalArgumentException("Failed to extract filename", e);
        }
    }

    @Override
    public MediaDetails importMedia(MediaLocation mediaLocation) {
        UUID newMediaId = resolveNewMediaId();
        MediaCategory mediaCategory = mediaCategoryResolver.resolve(mediaLocation);
        MediaType mimeType = fetchMediaType(mediaLocation);

        MediaData importedMedia = new MediaData(newMediaId, mediaCategory, mediaLocation.getSourceUrl(), mimeType);

        if (importedMedia.thumbnailPossible()) {
            MediaData thumbnailData = prepareThumbnail(importedMedia);
            importedMedia.setThumbnail(thumbnailData);
        }

        importedMedia = mediaDataRepository.save(importedMedia);

        return toMediaDetails(importedMedia);
    }

    private MediaType fetchMediaType(MediaLocation mediaLocation) {
        try {
            MediaMetadata metadata = mediaExternalClient.resolveMetadata(mediaLocation);
            if (metadata.getMimeType().isPresent())
                return metadata.getMimeType().get();
        }
        catch (MediaServiceUnavailableException e) {
            log.info("Media source service is not available: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public MediaDetails addMediaLink(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);
        mediaData.incrementLinkCount(1);

        return toMediaDetails(mediaData);
    }

    @Override
    public MediaDetails removeMediaLink(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);
        mediaData.incrementLinkCount(-1);

        return toMediaDetails(mediaData);
    }

    private MediaData loadMediaData(UUID mediaId) {
        return mediaDataRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Media with id %s not found", mediaId)));
    }

    private UUID resolveNewMediaId() {
        return UUID.randomUUID();
    }

    private MediaDetails toMediaDetails(MediaData mediaData) {
        MediaDetails.MediaDetailsBuilder builder = MediaDetails.builder()
                .mediaId(mediaData.getId())
                .category(mediaData.getCategory())
                .filename(mediaData.getOriginalFilename())
                .mimeType(mediaData.getMimeType().toString())
                .hasThumbnail(mediaData.hasThumbnail());

        if (mediaData.getSourceUrl() != null)
            builder.source(new MediaLocation(mediaData.getSourceUrl()));

        return builder.build();
    }

    private MediaUploadInfo toMediaUploadInfo(MediaUpload mediaUpload, int nextChunkNo) {
        return MediaUploadInfo.builder()
                .mediaId(mediaUpload.getMediaId())
                .filename(mediaUpload.getFilename())
                .mimeType(mediaUpload.getMimeType())
                .nextChunkNo(nextChunkNo)
                .build();
    }
}
