package eu.sshopencloud.marketplace.domain.media;

import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.Resource;

@Value
@Builder
class MediaFileHandle {
    Resource mediaFile;
    long fileSize;
}
