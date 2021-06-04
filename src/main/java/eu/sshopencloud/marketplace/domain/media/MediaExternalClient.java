package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.common.util.WebClientUtils;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalLong;


@Component
class MediaExternalClient {
    public static final long MEDIA_FILE_MAX_BYTES = 1024 * 1024 * 5; // 5 MB

    private static final int MEDIA_CONNECT_TIMEOUT_MS = 500;
    private static final int MEDIA_HEAD_READ_TIMEOUT_MS = 500;
    private static final int MEDIA_RESOURCE_READ_TIMEOUT_MS = 10000;

    private final WebClient headClient;
    private final WebClient mediaClient;


    public MediaExternalClient(WebClient.Builder webClientBuilder) {
        this.headClient = WebClientUtils.create(
                webClientBuilder, MEDIA_CONNECT_TIMEOUT_MS, MEDIA_HEAD_READ_TIMEOUT_MS, Optional.empty()
        );

        this.mediaClient = WebClientUtils.create(
                webClientBuilder, MEDIA_CONNECT_TIMEOUT_MS, MEDIA_RESOURCE_READ_TIMEOUT_MS, Optional.of(MEDIA_FILE_MAX_BYTES)
        );
    }


    public DownloadedMediaFile fetchMediaFile(MediaLocation mediaLocation) {
        try {
            Flux<DataBuffer> mediaContent = mediaClient.get()
                    .uri(mediaLocation.getSourceUrl().toURI())
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);

            return new DownloadedFluxMediaFile(mediaContent);
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException("Unexpected invalid media location url syntax", e);
        }
    }


    @Value
    @Builder
    public static class MediaInfo {
        Optional<MediaType> mimeType;
        Optional<String> filename;
        OptionalLong contentLength;
    }
}
