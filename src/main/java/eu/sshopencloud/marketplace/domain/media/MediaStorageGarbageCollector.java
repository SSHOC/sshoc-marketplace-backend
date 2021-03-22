package eu.sshopencloud.marketplace.domain.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@Component
class MediaStorageGarbageCollector {

    private final MediaFileStorage mediaFileStorage;
    private final MediaDataRepository mediaDataRepository;
    private final MediaUploadRepository mediaUploadRepository;

    private final Duration temporaryMediaRetention;
    private final Duration mediaUploadRetention;

    private final Clock clock;


    public MediaStorageGarbageCollector(MediaFileStorage mediaFileStorage,
                                        MediaDataRepository mediaDataRepository,
                                        MediaUploadRepository mediaUploadRepository,
                                        @Value("${marketplace.media.retention.temporary}") Duration temporaryMediaRetention,
                                        @Value("${marketplace.media.retention.upload}") Duration mediaUploadRetention,
                                        Clock clock) {

        this.mediaFileStorage = mediaFileStorage;
        this.mediaDataRepository = mediaDataRepository;
        this.mediaUploadRepository = mediaUploadRepository;

        this.temporaryMediaRetention = temporaryMediaRetention;
        this.mediaUploadRetention = mediaUploadRetention;
        this.clock = clock;
    }


    // Run with initial delay of 15 minutes and with delay of 30 minutes between invocations
    @Scheduled(fixedDelayString = "PT30M", initialDelayString = "PT15M")
    @Transactional
    public void handleMediaCleanup() {
        LocalDateTime mediaRetentionTimestamp = LocalDateTime.now(clock).minus(temporaryMediaRetention);
        Stream<MediaData> staleMedia = mediaDataRepository.streamStaleMedia(mediaRetentionTimestamp);

        deleteStaleMedia(staleMedia);

        LocalDateTime uploadRetentionTimestamp = LocalDateTime.now(clock).minus(mediaUploadRetention);
        Stream<MediaUpload> staleUploads = mediaUploadRepository.findAllByUpdatedBefore(uploadRetentionTimestamp);

        deleteStaleUploads(staleUploads);
    }

    private void deleteStaleMedia(Stream<MediaData> staleMedia) {
        List<MediaData> deleteBatch = new ArrayList<>();

        staleMedia.forEach(media -> {
            if (media.hasThumbnail())
                media.getThumbnail().decrementLinkCount();

            if (media.isStoredLocally())
                mediaFileStorage.cleanupMediaFile(media.getId());

            deleteBatch.add(media);
            if (deleteBatch.size() >= 10) {
                mediaDataRepository.deleteInBatch(deleteBatch);
                deleteBatch.clear();
            }
        });

        if (!deleteBatch.isEmpty())
            mediaDataRepository.deleteInBatch(deleteBatch);
    }

    private void deleteStaleUploads(Stream<MediaUpload> staleUploads) {
        staleUploads.forEach(upload -> {
            mediaFileStorage.cleanupFileUpload(upload.getMediaId(), true);
            mediaUploadRepository.delete(upload);
        });
    }
}
