package eu.sshopencloud.marketplace.domain.media;

import lombok.Builder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import javax.persistence.EntityNotFoundException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
class MediaFileStorage {
    private static final int MEDIA_CHUNK_BUFFER_SIZE = 1024 * 256;

    private final MediaFileUploadRepository fileUploadRepository;

    private final Path mediaFilesPath;
    private final Path mediaChunksPath;

    private final DataSize maxMediaSize;
    private final int maxChunksNumber;

    private final ConcurrentMap<UUID, Integer> processedChunks;


    public MediaFileStorage(MediaFileUploadRepository fileUploadRepository,
                            @Value("${marketplace.media.files.path}") Path mediaRootPath,
                            @Value("${marketplace.media.files.maxSize}") String maxFileSize,
                            @Value("${marketplace.media.files.maxChunks}") int maxChunksNumber) throws IOException {

        this.fileUploadRepository = fileUploadRepository;

        this.mediaFilesPath = Files.createDirectories(Path.of(mediaRootPath.toString(), "completed"));
        this.mediaChunksPath = Files.createDirectories(Path.of(mediaRootPath.toString(), "chunked"));

        this.maxMediaSize = DataSize.parse(maxFileSize, DataUnit.MEGABYTES);
        this.maxChunksNumber = maxChunksNumber;

        this.processedChunks = new ConcurrentHashMap<>();
    }


    public MediaFileHandle retrieveMediaFile(UUID mediaId) {
        Path mediaPath = getPathForMedia(mediaId);
        if (!Files.exists(mediaPath))
            throw new IllegalArgumentException("Media file does not exist");

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
        Path mediaFilePath = getPathForMedia(mediaId);
        long mediaSize = storeMediaFilePart(mediaFilePath, mediaResource);

        return MediaFileInfo.builder()
                .mediaId(mediaId)
                .mediaFilePath(mediaFilePath)
                .fileSize(mediaSize)
                .build();
    }

    public MediaChunkInfo storeFirstMediaChunk(UUID mediaId, Resource mediaChunk, int chunkNo) {
        if (chunkNo != 0)
            throw new IllegalArgumentException(String.format("First chunk must be numbered 0 (actual: %d)", chunkNo));

        if (processedChunks.putIfAbsent(mediaId, 0) != null || fileUploadRepository.existsById(mediaId))
            throw new IllegalArgumentException("Chunk upload has been already started");

        Path chunkDirectory = createDirectoryForChunks(mediaId);
        StoredChunk storedChunk = storeChunkFile(chunkDirectory, mediaChunk, chunkNo);
        Path chunkPath = storedChunk.getChunkPath();
        final int nextChunkNo = 1;

        MediaFileUpload fileUpload = new MediaFileUpload(mediaId, chunkDirectory);
        fileUpload.setNextChunkNo(nextChunkNo);
        fileUpload.incrementSize(storedChunk.getChunkSize());

        fileUploadRepository.save(fileUpload);

        return MediaChunkInfo.builder()
                .mediaId(mediaId)
                .chunksDirectory(chunkDirectory)
                .chunkPath(chunkPath)
                .nextChunkNo(nextChunkNo)
                .build();
    }

    private Path createDirectoryForChunks(UUID mediaId) {
        try {
            Path mediaDirectory = Path.of(mediaChunksPath.toString(), mediaId.toString());
            return Files.createDirectories(mediaDirectory);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to create files for media upload", e);
        }
    }

    public MediaChunkInfo storeNextMediaChunk(UUID mediaId, Resource mediaChunk, int chunkNo) {
        MediaFileUpload mediaUpload = loadMediaFileUpload(mediaId);

        if (chunkNo != mediaUpload.getNextChunkNo())
            throw new IllegalArgumentException("Illegal chunk number");

        if (!processedChunks.replace(mediaId, mediaUpload.getCurrentChunkNo(), chunkNo))
            throw new IllegalArgumentException("Broken media upload due to an unexpected error");

        if (chunkNo >= maxChunksNumber)
            throw new IllegalArgumentException("Maximum media chunks number limit exceeded");

        if (getMediaSize(mediaChunk) + mediaUpload.getCurrentSize() > maxMediaSize.toBytes())
            throw new IllegalArgumentException("Maximum media size limit exceeded");

        Path chunkDirectory = mediaUpload.getChunksDirectory();
        StoredChunk storedChunk = storeChunkFile(chunkDirectory, mediaChunk, chunkNo);

        mediaUpload.incrementChunkNo();
        mediaUpload.incrementSize(storedChunk.getChunkSize());

        return MediaChunkInfo.builder()
                .mediaId(mediaId)
                .chunksDirectory(chunkDirectory)
                .chunkPath(storedChunk.getChunkPath())
                .nextChunkNo(mediaUpload.getCurrentChunkNo())
                .build();
    }

    private StoredChunk storeChunkFile(Path chunkDirectory, Resource mediaChunk, int chunkNo) {
        Path chunkPath = getPathForChunk(chunkDirectory, chunkNo);
        long chunkSize = storeMediaFilePart(chunkPath, mediaChunk);

        return StoredChunk.builder()
                .chunkPath(chunkPath)
                .chunkSize(chunkSize)
                .build();
    }

    private Path getPathForChunk(Path chunksDirectory, int chunkNo) {
        String chunkFilename = StringUtils.leftPad(Integer.toString(chunkNo), 5, '0');
        return Path.of(chunksDirectory.toString(), chunkFilename);
    }

    public MediaFileInfo completeMediaUpload(UUID mediaId) {
        MediaFileUpload mediaUpload = loadMediaFileUpload(mediaId);
        Path chunksDirectory = mediaUpload.getChunksDirectory();

        try {
            Path mediaPath = Files.createFile(getPathForMedia(mediaId));

            try (OutputStream writeStream = new BufferedOutputStream(new FileOutputStream(mediaPath.toFile()))) {

                for (int chunkNo = 0; chunkNo < mediaUpload.chunksNumber(); ++chunkNo) {
                    Path chunkPath = getPathForChunk(chunksDirectory, chunkNo);

                    try (InputStream copyStream = new BufferedInputStream(new FileInputStream(chunkPath.toFile()))) {
                        IOUtils.copy(copyStream, writeStream, MEDIA_CHUNK_BUFFER_SIZE);
                    }
                }
            }

            cleanupMediaChunks(mediaId);

            long mediaSize = Files.size(mediaPath);

            return MediaFileInfo.builder()
                    .mediaId(mediaId)
                    .mediaFilePath(mediaPath)
                    .fileSize(mediaSize)
                    .build();
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to save media file", e);
        }
    }

    private void cleanupMediaChunks(UUID mediaId) {
        // TODO
    }

    private MediaFileUpload loadMediaFileUpload(UUID mediaId) {
        return fileUploadRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No ongoing media upload with id: %s", mediaId)));
    }

    private long storeMediaFilePart(Path filePath, Resource mediaResource) {
        validateMediaSize(mediaResource);

        try {
            FileUtils.copyInputStreamToFile(mediaResource.getInputStream(), filePath.toFile());
            return Files.size(filePath);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to save media file", e);
        }
    }

    private void validateMediaSize(Resource mediaResource) {
        if (getMediaSize(mediaResource) > maxMediaSize.toBytes())
            throw new IllegalArgumentException("Maximum media size limit exceeded");
    }

    private long getMediaSize(Resource mediaResource) {
        try {
            return mediaResource.contentLength();
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read content length of the media file", e);
        }
    }

    private Path getPathForMedia(UUID mediaId) {
        return Path.of(mediaFilesPath.toString(), mediaId.toString());
    }

    @lombok.Value
    @Builder
    private static class StoredChunk {
        Path chunkPath;
        long chunkSize;
    }
}
