package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.Builder;
import lombok.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;


@Component
class MediaExternalClient {
    private static final int MEDIA_EXTERNAL_CONNECT_TIMEOUT_MS = 100;
    private static final int MEDIA_EXTERNAL_READ_TIMEOUT_MS = 100;

    private final RestTemplate restTemplate;


    public MediaExternalClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(MEDIA_EXTERNAL_CONNECT_TIMEOUT_MS))
                .setReadTimeout(Duration.ofMillis(MEDIA_EXTERNAL_READ_TIMEOUT_MS))
                .build();
    }


    @Cacheable(cacheNames = "mediaMetadata", sync = true)
    public MediaMetadata resolveMetadata(MediaLocation mediaLocation) throws MediaServiceUnavailableException {
        try {
            HttpHeaders headers = restTemplate.headForHeaders(mediaLocation.getSourceUrl().toURI());

            MediaType mimeType = headers.getContentType();
            String filename = headers.getContentDisposition().getFilename();

            return MediaMetadata.builder()
                    .mimeType(Optional.ofNullable(mimeType))
                    .filename(Optional.ofNullable(filename))
                    .build();
        }
        catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Media resource is not available in the given service", e);
        }
        catch (RestClientException e) {
            throw new MediaServiceUnavailableException("Failed to connect to the media service", e);
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
    }
}
