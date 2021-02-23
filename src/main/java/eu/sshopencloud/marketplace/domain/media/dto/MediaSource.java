package eu.sshopencloud.marketplace.domain.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
public class MediaSource {
    private String sourceUrl;
}
