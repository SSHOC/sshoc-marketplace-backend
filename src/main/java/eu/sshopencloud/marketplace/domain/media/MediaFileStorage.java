package eu.sshopencloud.marketplace.domain.media;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


@Component
class MediaFileStorage {
    private final Path mediaFilesPath;
    private final Path mediaChunksPath;


    public MediaFileStorage(@Value("${marketplace.media.path}") Path mediaRootPath) throws IOException {
        this.mediaFilesPath = Files.createDirectories(Path.of(mediaRootPath.toString(), "completed"));
        this.mediaChunksPath = Files.createDirectories(Path.of(mediaRootPath.toString(), "chunked"));
    }

    public MediaFileInfo storeMediaFile(UUID mediaId, Resource mediaResource) throws IOException {
        Path mediaFilePath = getPathForMedia(mediaId);
        FileUtils.copyInputStreamToFile(mediaResource.getInputStream(), mediaFilePath.toFile());

        return new MediaFileInfo(mediaId, mediaFilePath);
    }

    private Path getPathForMedia(UUID mediaId) {
        return Path.of(mediaFilesPath.toString(), mediaId.toString());
    }

}
