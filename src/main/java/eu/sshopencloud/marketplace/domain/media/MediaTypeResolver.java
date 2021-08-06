package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.MediaExternalClient.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
class MediaTypeResolver {

    private final MediaSourceService mediaSourceService;
    private final MediaExternalClient mediaExternalClient;


    public MediaCategory resolve(MediaLocation mediaLocation) {
        URL mediaSourceUrl = mediaLocation.getSourceUrl();
        System.out.println(mediaSourceService.getAllMediaSources());

        Optional<MediaSource> mediaSource = mediaSourceService.resolveMediaSource(mediaSourceUrl);

        if (mediaSource.isPresent())
            return mediaSource.get().getMediaCategory();

        try {
            MediaInfo mediaInfo = mediaExternalClient.resolveMediaInfo(mediaLocation);

            if (mediaInfo.getMimeType().isPresent()) {
                MediaType mimeType = mediaInfo.getMimeType().get();
                Optional<MediaCategory> category = resolveByMimeType(mimeType);

                if (category.isPresent())
                    return category.get();
            }

            if (mediaInfo.getFilename().isPresent())
                return resolveCategoryByFilename(mediaInfo.getFilename().get());
        }
        catch (MediaServiceUnavailableException e) {
            log.info(String.format("Service for media URL is not available: %s", mediaLocation.getSourceUrl().toString()), e);
        }

        return MediaCategory.OBJECT;
    }

    public MediaCategory resolve(Optional<MediaType> mimeType, String filename) {
        return mimeType.flatMap(this::resolveByMimeType)
                .orElseGet(() -> resolveCategoryByFilename(filename));
    }

    public Optional<MediaType> resolveMimeType(String filename) {
        return MimeTypeByFilenameUtils.resolveMimeTypeByFilename(filename);
    }

    private MediaCategory resolveCategoryByFilename(String filename) {
        return MimeTypeByFilenameUtils.resolveMimeTypeByFilename(filename)
                .flatMap(this::resolveByMimeType)
                .orElse(MediaCategory.OBJECT);
    }

    private Optional<MediaCategory> resolveByMimeType(MediaType mimeType) {
        MediaType imageType = new MediaType("image");

        if (imageType.includes(mimeType))
            return Optional.of(MediaCategory.IMAGE);

        return Optional.empty();
    }

}
