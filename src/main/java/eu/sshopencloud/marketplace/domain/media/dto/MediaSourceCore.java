package eu.sshopencloud.marketplace.domain.media.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaSourceCore {
    private String code;
    private String serviceUrl;
    private MediaCategory mediaCategory;
    private int ord;
}
