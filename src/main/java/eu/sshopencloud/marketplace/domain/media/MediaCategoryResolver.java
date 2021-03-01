package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
class MediaCategoryResolver {

    public MediaCategory resolve(Optional<MediaType> mimeType, String filename) {
        return mimeType.flatMap(this::resolveByMimeType)
                .orElseGet(() -> resolveByFilename(filename));
    }

    private Optional<MediaCategory> resolveByMimeType(MediaType mimeType) {
        MediaType imageType = new MediaType("image");

        if (imageType.includes(mimeType))
            return Optional.of(MediaCategory.IMAGE);

        return Optional.of(MediaCategory.OBJECT);
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
