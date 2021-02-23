package eu.sshopencloud.marketplace.domain.media;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


@Component
class MediaFileStorage {
    private final Path mediaFilesPath;
    private final Path mediaChunksPath;
    private final DataSize maxMediaSize;


    public MediaFileStorage(@Value("${marketplace.media.files.path}") Path mediaRootPath,
                            @Value("${marketplace.media.files.maxSize") String maxFileSize) throws IOException {

        this.mediaFilesPath = Files.createDirectories(Path.of(mediaRootPath.toString(), "completed"));
        this.mediaChunksPath = Files.createDirectories(Path.of(mediaRootPath.toString(), "chunked"));

        this.maxMediaSize = DataSize.parse(maxFileSize, DataUnit.MEGABYTES);
    }


    public MediaFileHandle retrieveMediaFile(UUID mediaId) {
        Path mediaPath = getPathForMedia(mediaId);
        if (!Files.exists(mediaPath))
            throw new IllegalArgumentException("Media file does not exist"); // TODO

        try {
            return MediaFileHandle.builder()
                    .mediaFile(new FileSystemResource(mediaPath))
                    .fileSize(Files.size(mediaPath))
                    .build();
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to access media file", e);
        }
    }

    public MediaFileInfo storeMediaFile(UUID mediaId, Resource mediaResource) {
        validateMediaSize(mediaResource);

        try {
            Path mediaFilePath = getPathForMedia(mediaId);
            FileUtils.copyInputStreamToFile(mediaResource.getInputStream(), mediaFilePath.toFile());

            return new MediaFileInfo(mediaId, mediaFilePath);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to save media file", e);
        }
    }

    private void validateMediaSize(Resource mediaResource) {
        try {
            if (mediaResource.contentLength() > maxMediaSize.toBytes())
                throw new IllegalArgumentException("Media file size exceeds the maximum limit");
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read content length of the media file", e);
        }
    }

    private Path getPathForMedia(UUID mediaId) {
        return Path.of(mediaFilesPath.toString(), mediaId.toString());
    }
}
