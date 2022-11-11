package eu.sshopencloud.marketplace.domain.media;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.util.Optional;

@UtilityClass
public class MimeTypeByFilenameUtils {

    public Optional<MediaType> resolveMimeTypeByFilename(String filename) {
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
            case "mp4":
                return Optional.of(new MediaType("video", "mp4"));
            case "mov":
            case "qt":
                return Optional.of(new MediaType("video", "quicktime"));
            case "wmv":
                return Optional.of(new MediaType("video", "x-ms-wmv"));
            case "avi":
                return Optional.of(new MediaType("video", "x-msvideo"));
            case "flv":
                return Optional.of(new MediaType("video", "x-flv"));
        }

        return Optional.empty();
    }
}
