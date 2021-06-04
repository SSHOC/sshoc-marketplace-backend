package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.MediaExternalClient.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
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
@RequiredArgsConstructor
@Slf4j
class MediaTypeResolver {

    private final MediaSourceService mediaSourceService;
    private final MediaExternalClient mediaExternalClient;

    public MediaCategory resolve(MediaLocation mediaLocation) {
        URL mediaSourceUrl = mediaLocation.getSourceUrl();
        Optional<MediaSource> mediaSource = mediaSourceService.resolveMediaSource(mediaSourceUrl);

        if (mediaSource.isPresent())
            return mediaSource.get().getMediaCategory();

        try {

            MediaInfo mediaInfo = resolveMediaInfo(mediaLocation);

            if (mediaInfo.getMimeType().isPresent()) {
                MediaType mimeType = mediaInfo.getMimeType().get();
                Optional<MediaCategory> category = resolveByMimeType(mimeType);

                if (category.isPresent())
                    return category.get();
            }

            if (mediaInfo.getFilename().isPresent())
                return resolveCategoryByFilename(mediaInfo.getFilename().get());
        } catch (MediaServiceUnavailableException e) {
            log.info(String.format("Service for media URL is not available: %s", mediaLocation.getSourceUrl().toString()), e);
        }

        return MediaCategory.OBJECT;
    }

    public MediaCategory resolve(Optional<MediaType> mimeType, String filename) {
        return mimeType.flatMap(this::resolveByMimeType)
                .orElseGet(() -> resolveCategoryByFilename(filename));
    }

    public Optional<MediaType> resolveMimeType(String filename) {
        return resolveMimeTypeByFilename(filename);
    }

    private MediaCategory resolveCategoryByFilename(String filename) {
        return resolveMimeTypeByFilename(filename)
                .flatMap(this::resolveByMimeType)
                .orElse(MediaCategory.OBJECT);
    }

    private Optional<MediaCategory> resolveByMimeType(MediaType mimeType) {
        MediaType imageType = new MediaType("image");

        if (imageType.includes(mimeType))
            return Optional.of(MediaCategory.IMAGE);

        return Optional.empty();
    }

    private Optional<MediaType> resolveMimeTypeByFilename(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        if (StringUtils.isBlank(extension))
            return Optional.of(MediaType.APPLICATION_OCTET_STREAM);

        switch (extension.toLowerCase()) {
            case "jfif":
            case "jpg":
            case "jps":
            case "jpeg":
                return Optional.of(MediaType.IMAGE_JPEG);
            case "png":
                return Optional.of(MediaType.IMAGE_PNG);
            case "gif":
                return Optional.of(MediaType.IMAGE_GIF);
            case "bmp":
                return Optional.of(new MediaType("image", "bmp"));
            case "ico":
                return Optional.of(new MediaType("image", "vnd.microsoft.icon"));
            case "tif":
            case "tiff":
                return Optional.of(new MediaType("image", "tiff"));
            case "svg":
                return Optional.of(new MediaType("image", "svg+xml"));
            case "webp":
                return Optional.of(new MediaType("image", "webp"));
        }

        return Optional.empty();
    }


    @Cacheable(cacheNames = "mediaMetadata", sync = true)
    public MediaInfo resolveMediaInfo(MediaLocation mediaLocation) throws MediaServiceUnavailableException {
        try {

            byte[] bytes = mediaLocation.getSourceUrl().openStream().readAllBytes();
            ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
            String mediaFormat = "";
            String filename = Paths.get(new URI(String.valueOf(mediaLocation.getSourceUrl())).getPath()).getFileName().toString();
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                mediaFormat = reader.getFormatName();
            }

            return MediaInfo.builder()
                    .mimeType(resolveMimeTypeByFilename(mediaFormat))
                    .filename(Optional.ofNullable(filename))
                    .contentLength(OptionalLong.of(Long.valueOf(bytes.length)))
                    .build();
        } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unexpected invalid media location url syntax", e);
        }
    }


}
