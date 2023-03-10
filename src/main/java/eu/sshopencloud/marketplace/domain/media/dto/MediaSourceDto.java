package eu.sshopencloud.marketplace.domain.media.dto;

import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class MediaSourceDto {
    private String code;
    private String serviceUrl;
    private MediaCategory mediaCategory;
    private int ord;
}
