package eu.sshopencloud.marketplace.domain.media.exception;

import lombok.Getter;

import java.util.UUID;

public class MediaNotAvailableException extends Exception {

    @Getter
    private final UUID mediaId;


    public MediaNotAvailableException(UUID mediaId) {
        super(formatMessage(mediaId));
        this.mediaId = mediaId;
    }

    public MediaNotAvailableException(UUID mediaId, Throwable cause) {
        super(formatMessage(mediaId), cause);
        this.mediaId = mediaId;
    }

    private static String formatMessage(UUID mediaId) {
        return String.format("Media with id %s is not available", mediaId);
    }
}
