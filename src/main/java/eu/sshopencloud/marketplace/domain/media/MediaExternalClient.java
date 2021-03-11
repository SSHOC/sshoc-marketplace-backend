package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.common.util.WebClientUtils;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.Builder;
import lombok.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.OptionalLong;


@Component
class MediaExternalClient {
    public static final long MEDIA_FILE_MAX_BYTES = 1024 * 1024 * 5; // 5 MB

    private static final int MEDIA_CONNECT_TIMEOUT_MS = 100;
    private static final int MEDIA_HEAD_READ_TIMEOUT_MS = 100;
    private static final int MEDIA_RESOURCE_READ_TIMEOUT_MS = 5000;

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


    @Cacheable(cacheNames = "mediaMetadata", sync = true)
    public MediaMetadata resolveMetadata(MediaLocation mediaLocation) throws MediaServiceUnavailableException {
        try {
            ClientResponse response = headClient.head()
                    .uri(mediaLocation.getSourceUrl().toURI())
                    .exchange()
                    .block();

            if (response == null)
                throw new IllegalStateException("Failed to get a response from the media service");

            if (response.statusCode().is4xxClientError())
                throw new IllegalArgumentException("Media resource client error", response.createException().block());

            if (response.statusCode().isError())
                throw new MediaServiceUnavailableException("Failed to get a response from the media service", response.createException().block());

            ClientResponse.Headers headers = response.headers();
            String filename = headers.asHttpHeaders().getContentDisposition().getFilename();

            return MediaMetadata.builder()
                    .mimeType(headers.contentType())
                    .filename(Optional.ofNullable(filename))
                    .contentLength(headers.contentLength())
                    .build();
        }
        catch (URISyntaxException e) {
            throw new IllegalStateException("Unexpected invalid media location url syntax", e);
        }
    }

    @Value
    @Builder
    public static class MediaMetadata {
        Optional<MediaType> mimeType;
        Optional<String> filename;
        OptionalLong contentLength;
    }
}
