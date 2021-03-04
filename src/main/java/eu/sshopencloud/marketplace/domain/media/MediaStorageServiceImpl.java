package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.*;
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
class MediaStorageServiceImpl implements MediaStorageService {

    private final MediaFileStorage mediaFileStorage;
    private final MediaThumbnailService mediaThumbnailService;
    private final MediaCategoryResolver mediaCategoryResolver;
    private final MediaDataRepository mediaDataRepository;
    private final MediaUploadRepository mediaUploadRepository;
    private final int maxFilenameLength;


    public MediaStorageServiceImpl(MediaFileStorage mediaFileStorage,
                                   MediaThumbnailService mediaThumbnailService,
                                   MediaCategoryResolver mediaCategoryResolver,
                                   MediaDataRepository mediaDataRepository,
                                   MediaUploadRepository mediaUploadRepository,
                                   @Value("${marketplace.media.files.maxFilenameLength}") int maxFilenameLength) {

        this.mediaFileStorage = mediaFileStorage;
        this.mediaThumbnailService = mediaThumbnailService;
        this.mediaCategoryResolver = mediaCategoryResolver;
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
            throw new EntityNotFoundException(String.format("Thumbnail for requested media with id %s is not present", mediaId));

        MediaData thumbnail = mediaData.getThumbnail();
        return retrieveDownloadData(thumbnail);
    }

    private MediaDownload retrieveDownloadData(MediaData media) {
        MediaFileHandle mediaHandle = mediaFileStorage.retrieveMediaFile(media.getId());
        MediaType mediaType = (media.getMimeType() != null) ? MediaType.parseMediaType(media.getMimeType()) : null;

        return MediaDownload.builder()
                .mediaFile(mediaHandle.getMediaFile())
                .filename(media.getOriginalFilename())
                .mimeType(mediaType)
                .build();
    }

    @Override
    public MediaDetails saveCompleteMedia(Resource mediaFile, Optional<MediaType> mimeType) {
        UUID mediaId = resolveNewMediaId();
        MediaFileInfo fileInfo = mediaFileStorage.storeMediaFile(mediaId, mediaFile);

        String mediaFilename = extractFilename(mediaFile);
        MediaData mediaData = saveMediaData(fileInfo, mediaFilename, mimeType);

        return toMediaDetails(mediaData);
    }

    private MediaData saveMediaData(MediaFileInfo mediaFileInfo, String mediaFilename, Optional<MediaType> mimeType) {
        UUID mediaId = mediaFileInfo.getMediaId();
        MediaCategory mediaCategory = mediaCategoryResolver.resolve(mimeType, mediaFilename);
        String mediaMimeType = mimeType.map(MediaType::toString).orElse(null);

        MediaData mediaData = new MediaData(mediaId, mediaCategory, mediaFileInfo.getMediaFilePath(), mediaFilename, mediaMimeType);
        return mediaDataRepository.save(mediaData);
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
        MediaData mediaData = saveMediaData(mediaFileInfo, mediaUpload.getFilename(), mimeType);

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
        throw new UnsupportedOperationException("Not implemented");
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
                .mimeType(mediaData.getMimeType())
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
