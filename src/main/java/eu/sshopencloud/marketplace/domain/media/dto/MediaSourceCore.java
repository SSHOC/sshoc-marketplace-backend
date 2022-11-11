package eu.sshopencloud.marketplace.domain.media.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.sshopencloud.marketplace.domain.media.MediaCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaSourceCore {
    private String code;
    private String serviceUrl;
    private MediaCategory mediaCategory;
    private Integer ord;
}
