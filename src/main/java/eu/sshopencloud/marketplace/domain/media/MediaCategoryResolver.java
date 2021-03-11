package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.MediaExternalClient.MediaMetadata;
import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
class MediaCategoryResolver {

    private final MediaSourceService mediaSourceService;
    private final MediaExternalClient mediaExternalClient;


    public MediaCategory resolve(MediaLocation mediaLocation) {
        URL mediaSourceUrl = mediaLocation.getSourceUrl();
        Optional<MediaSource> mediaSource = mediaSourceService.resolveMediaSource(mediaSourceUrl);

        if (mediaSource.isPresent())
            return mediaSource.get().getMediaCategory();

        try {
            MediaMetadata mediaMetadata = mediaExternalClient.resolveMetadata(mediaLocation);

            if (mediaMetadata.getMimeType().isPresent()) {
                MediaType mimeType = mediaMetadata.getMimeType().get();
                Optional<MediaCategory> category = resolveByMimeType(mimeType);

                if (category.isPresent())
                    return category.get();
            }

            if (mediaMetadata.getFilename().isPresent())
                return resolveByFilename(mediaMetadata.getFilename().get());
        }
        catch (MediaServiceUnavailableException e) {
            log.info(String.format("Service for media URL is not available: %s", mediaLocation.getSourceUrl().toString()), e);
        }

        return MediaCategory.OBJECT;
    }

    public MediaCategory resolve(Optional<MediaType> mimeType, String filename) {
        return mimeType.flatMap(this::resolveByMimeType)
                .orElseGet(() -> resolveByFilename(filename));
    }

    private Optional<MediaCategory> resolveByMimeType(MediaType mimeType) {
        MediaType imageType = new MediaType("image");

        if (imageType.includes(mimeType))
            return Optional.of(MediaCategory.IMAGE);

        return Optional.empty();
    }

    private MediaCategory resolveByFilename(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        if (StringUtils.isBlank(extension))
            return MediaCategory.OBJECT;

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return MediaCategory.IMAGE;

            default:
                return MediaCategory.OBJECT;
        }
    }
}
