package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.common.util.WebClientUtils;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.Builder;
import lombok.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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


    @Cacheable(cacheNames = "mediaMetadata", sync = true)
    public MediaInfo resolveMediaInfo(MediaLocation mediaLocation) throws MediaServiceUnavailableException {
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

            if (headers.contentType().isEmpty()) {

                byte[] bytes = fetchMediaFile(mediaLocation).consumeFile(this::generateByteArray);
                Iterator<ImageReader> readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)));

                String mediaFormat = "";
                filename = Paths.get(new URI(String.valueOf(mediaLocation.getSourceUrl())).getPath()).getFileName().toString();

                if(readers.hasNext()) {
                    ImageReader reader = readers.next();
                    mediaFormat = reader.getFormatName();

                }
                return MediaInfo.builder()
                        .mimeType(MimeTypeByFilenameUtils.resolveMimeTypeByFilename("." + mediaFormat))
                        .filename(Optional.ofNullable(filename))
                        .contentLength(OptionalLong.of(Long.valueOf(bytes.length)))
                        .build();
            } else
                return MediaInfo.builder()
                        .mimeType(headers.contentType())
                        .filename(Optional.ofNullable(filename))
                        .contentLength(headers.contentLength())
                        .build();
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unexpected invalid media location url syntax", e);
        }
    }

    public DownloadedMediaFile fetchMediaFile(MediaLocation mediaLocation) {
        try {
            Flux<DataBuffer> mediaContent = mediaClient.get()
                    .uri(mediaLocation.getSourceUrl().toURI())
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);


            return new DownloadedFluxMediaFile(mediaContent);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unexpected invalid media location url syntax", e);
        }
    }

    public byte[] generateByteArray(InputStream mediaStream) {
        try {
            return mediaStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        } return null;
    }

    @Value
    @Builder
    public static class MediaInfo {
        Optional<MediaType> mimeType;
        Optional<String> filename;
        OptionalLong contentLength;
    }
}
