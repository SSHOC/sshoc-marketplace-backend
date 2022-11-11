package eu.sshopencloud.marketplace.domain.media.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;


@Value
@Builder
public class MediaDownload {
    Resource mediaFile;
    long contentLength;
    MediaType mimeType;
    String filename;
}
