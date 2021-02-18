package eu.sshopencloud.marketplace.domain.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
public class MediaInfo {
    private UUID mediaId;
}
