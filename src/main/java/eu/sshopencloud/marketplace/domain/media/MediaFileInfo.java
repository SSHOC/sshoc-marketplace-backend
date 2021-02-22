package eu.sshopencloud.marketplace.domain.media;

import lombok.Value;

import java.nio.file.Path;
import java.util.UUID;

@Value
class MediaFileInfo {
    UUID mediaId;
    Path mediaFilePath;
}
