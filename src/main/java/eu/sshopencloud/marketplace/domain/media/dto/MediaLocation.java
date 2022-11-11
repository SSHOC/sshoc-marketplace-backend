package eu.sshopencloud.marketplace.domain.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaLocation {
    private URL sourceUrl;
}
