package eu.sshopencloud.marketplace.domain.media;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.util.UUID;


@Value
@Builder
class MediaChunkInfo {
    UUID mediaId;
    Path chunksDirectory;
    Path chunkPath;

    int nextChunkNo;
}
