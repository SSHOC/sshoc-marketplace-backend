package eu.sshopencloud.marketplace.domain.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.net.URL;


@Data
@Builder
@AllArgsConstructor
public class MediaLocation {
    private URL sourceUrl;
}
