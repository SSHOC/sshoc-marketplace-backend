package eu.sshopencloud.marketplace.domain.media.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaInfo {
    private UUID mediaId;
    private MediaCategory category;
    private String sourceUrl;
    private String filename;
    private String mimeType;
    private boolean hasThumbnail;
}
