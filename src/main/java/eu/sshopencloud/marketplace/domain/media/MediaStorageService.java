package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.MediaExternalClient.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.*;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Service
@Transactional
@Slf4j
public class MediaStorageService {

    private final MediaFileStorage mediaFileStorage;
    private final MediaThumbnailService mediaThumbnailService;
    private final MediaTypeResolver mediaTypeResolver;
    private final MediaExternalClient mediaExternalClient;

    private final MediaDataRepository mediaDataRepository;
    private final MediaUploadRepository mediaUploadRepository;

    private final int maxFilenameLength;


    public MediaStorageService(MediaFileStorage mediaFileStorage,
                               MediaThumbnailService mediaThumbnailService,
                               MediaTypeResolver mediaTypeResolver,
                               MediaExternalClient mediaExternalClient,
                               MediaDataRepository mediaDataRepository,
                               MediaUploadRepository mediaUploadRepository,
                               @Value("${marketplace.media.files.maxFilenameLength}") int maxFilenameLength) {

        this.mediaFileStorage = mediaFileStorage;
        this.mediaThumbnailService = mediaThumbnailService;
        this.mediaTypeResolver = mediaTypeResolver;
        this.mediaExternalClient = mediaExternalClient;

        this.mediaDataRepository = mediaDataRepository;
        this.mediaUploadRepository = mediaUploadRepository;

        this.maxFilenameLength = maxFilenameLength;
    }


    public MediaDownload getMediaForDownload(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);

        if (!mediaData.isStoredLocally())
            throw new IllegalArgumentException("Requested media file comes from an external source");

        return retrieveDownloadData(mediaData);
    }

    public MediaDownload getThumbnailForDownload(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);

        if (!mediaData.hasThumbnail())
            throw new EntityNotFoundException(String.format("Thumbnail for media with id %s is not present", mediaId));

        MediaData thumbnail = mediaData.getThumbnail();
        return retrieveDownloadData(thumbnail);
    }

    private MediaDownload retrieveDownloadData(MediaData media) {
        MediaFileHandle mediaHandle = mediaFileStorage.retrieveMediaFile(media.getId());

        return MediaDownload.builder()
                .mediaFile(mediaHandle.getMediaFile())
                .filename(media.getOriginalFilename())
                .contentLength(mediaHandle.getFileSize())
                .mimeType(media.getMimeType())
                .build();
    }

    public boolean ensureMediaAvailable(UUID mediaId) {
        return mediaDataRepository.existsById(mediaId);
    }

    public MediaDetails getMediaDetails(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);
        return toMediaDetails(mediaData);
    }

    public MediaDetails saveCompleteMedia(Resource mediaFile, Optional<MediaType> mimeType) {

        UUID mediaId = resolveNewMediaId();
        MediaFileInfo fileInfo = mediaFileStorage.storeMediaFile(mediaId, mediaFile);

        String mediaFilename = extractFilename(mediaFile, true);
        MediaData mediaData = prepareMediaData(fileInfo, mediaFilename, mimeType);

        if (mediaData.thumbnailPossible()) {
            try {
                MediaData thumbnailData = prepareThumbnail(mediaData.getFilePath());
                mediaData.setThumbnail(thumbnailData);
            }
            catch (ThumbnailGenerationException e) {
                log.info("Failed to generate thumbnail for media: {} ({})", mediaId, mediaFilename);
            }
        }

        mediaData = mediaDataRepository.save(mediaData);

        return toMediaDetails(mediaData);
    }

    private MediaData prepareThumbnail(Path mediaFilePath) {
        Resource thumbnailFile = mediaThumbnailService.generateThumbnail(mediaFilePath);
        return prepareThumbnail(thumbnailFile);
    }

    private MediaData prepareThumbnail(MediaLocation mediaLocation) {
        Resource thumbnailFile = mediaThumbnailService.generateThumbnail(mediaLocation);
        return prepareThumbnail(thumbnailFile);
    }

    private MediaData prepareThumbnail(Resource thumbnailFile) {
        UUID thumbnailId = resolveNewMediaId();
        MediaFileInfo thumbnailInfo = mediaFileStorage.storeMediaFile(thumbnailId, thumbnailFile);

        String thumbnailFilename = mediaThumbnailService.getDefaultThumbnailFilename();
        return (new MediaData(
                thumbnailId, MediaCategory.THUMBNAIL, thumbnailInfo.getMediaFilePath(), thumbnailFilename, MediaType.IMAGE_JPEG));

    }

    private MediaData prepareMediaData(MediaFileInfo mediaFileInfo, String mediaFilename, Optional<MediaType> mimeType) {
        UUID mediaId = mediaFileInfo.getMediaId();
        MediaCategory mediaCategory = mediaTypeResolver.resolve(mimeType, mediaFilename);
        MediaType mediaMimeType = mimeType.orElseGet(() -> mediaTypeResolver.resolveMimeType(mediaFilename).orElse(null));

        return new MediaData(mediaId, mediaCategory, mediaFileInfo.getMediaFilePath(), mediaFilename, mediaMimeType);
    }

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
            String mediaFilename = extractFilename(mediaChunk, false);
            String mediaType = mimeType.map(MimeType::toString).orElse(null);

            MediaUpload mediaUpload = new MediaUpload(newMediaId, mediaFilename, mediaType);
            MediaChunkInfo chunkInfo = mediaFileStorage.storeFirstMediaChunk(newMediaId, mediaChunk, chunkNo);

            mediaUpload = mediaUploadRepository.save(mediaUpload);

            return toMediaUploadInfo(mediaUpload, chunkInfo.getNextChunkNo());
        }
    }

    public MediaDetails completeMediaUpload(UUID mediaId, Optional<String> filename) {
        MediaUpload mediaUpload = mediaUploadRepository.findByMediaId(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media upload with id %s not found"));

        MediaFileInfo mediaFileInfo = mediaFileStorage.completeMediaUpload(mediaId);
        String mediaFilename = filename.orElseGet(() -> {
            if (StringUtils.isBlank(mediaUpload.getFilename()))
                throw new IllegalArgumentException("Filename not present");

            return mediaUpload.getFilename();
        });

        Optional<MediaType> mimeType = Optional.ofNullable(mediaUpload.getMimeType()).map(MediaType::parseMediaType);
        MediaData mediaData = prepareMediaData(mediaFileInfo, mediaFilename, mimeType);

        if (mediaData.thumbnailPossible()) {
            MediaData thumbnailData = prepareThumbnail(mediaData.getFilePath());
            mediaData.setThumbnail(thumbnailData);
        }

        mediaData = mediaDataRepository.save(mediaData);

        return toMediaDetails(mediaData);
    }

    private String extractFilename(Resource mediaFile, boolean required) {
        String filename = mediaFile.getFilename();

        if (StringUtils.isBlank(filename)) {
            if (required)
                throw new IllegalArgumentException("Filename not present");

            return null;
        }

        if (filename.length() > maxFilenameLength)
            throw new IllegalArgumentException("Filename too long");

        return filename;
    }

    public MediaDetails importMedia(MediaLocation mediaLocation) {
        UUID newMediaId = resolveNewMediaId();

        MediaCategory mediaCategory = mediaTypeResolver.resolve(mediaLocation);
        MediaType mimeType = fetchMediaType(mediaLocation);

        MediaData importedMedia = new MediaData(newMediaId, mediaCategory, mediaLocation.getSourceUrl(), mimeType);

        if (importedMedia.thumbnailPossible()) {
            try {
                MediaData thumbnailData = prepareThumbnail(mediaLocation);
                importedMedia.setThumbnail(thumbnailData);
            }
            catch (ThumbnailGenerationException e) {
                log.info("Failed to generate thumbnail from location: {}", mediaLocation.getSourceUrl());
            }
        }

        importedMedia = mediaDataRepository.save(importedMedia);

        return toMediaDetails(importedMedia);
    }

    private MediaType fetchMediaType(MediaLocation mediaLocation) {
        try {
            MediaInfo mediaInfo = mediaExternalClient.resolveMediaInfo(mediaLocation);

            if (Objects.isNull(mediaInfo.getMimeType()) || !mediaInfo.getMimeType().isPresent()) {

                URLConnection conn = mediaLocation.getSourceUrl().openConnection();
                return MediaType.parseMediaType(conn.getContentType());
            }else{
                return mediaInfo.getMimeType().get();
            }
        }
        catch (MediaServiceUnavailableException e) {
            log.info("Media source service is not available: {}", e.getMessage());
        } catch (IOException e) {
            log.info("Url connection not found", e.getMessage());
        }

        return null;
    }

    public MediaDetails linkToMedia(UUID mediaId) throws MediaNotAvailableException {
        Optional<MediaData> mediaData = mediaDataRepository.findById(mediaId);

        if (mediaData.isEmpty())
            throw new MediaNotAvailableException(mediaId);

        mediaData.get().incrementLinkCount();

        return toMediaDetails(mediaData.get());
    }

    public MediaDetails removeMediaLink(UUID mediaId) {
        MediaData mediaData = loadMediaData(mediaId);
        mediaData.decrementLinkCount();

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
                .hasThumbnail(mediaData.hasThumbnail());

        if (mediaData.getMimeType() != null)
            builder = builder.mimeType(mediaData.getMimeType().toString());

        if (mediaData.getSourceUrl() != null)
            builder = builder.location(new MediaLocation(mediaData.getSourceUrl()));

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
