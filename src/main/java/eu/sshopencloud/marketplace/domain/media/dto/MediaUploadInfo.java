package eu.sshopencloud.marketplace.domain.media.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;


@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaUploadInfo {
    UUID mediaId;
    String filename;
    String mimeType;
    int nextChunkNo;
}
