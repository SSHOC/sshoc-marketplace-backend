package eu.sshopencloud.marketplace.domain.media;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.UUID;

@Value
@Builder
class MediaFileInfo {
    UUID mediaId;
    Path mediaFilePath;
    long fileSize;
}
