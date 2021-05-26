package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
@Slf4j
class MediaThumbnailService {
    private static final String DEFAULT_THUMBNAIL_FILENAME = "thumb.jpg";
    private static final String THUMBNAIL_FILE_EXTENSION = "jpg";

    private final MediaExternalClient mediaExternalClient;
    private final int thumbnailWidth;
    private final int thumbnailHeight;


    public MediaThumbnailService(MediaExternalClient mediaExternalClient,
                                 @Value("${marketplace.media.thumbnail.size.width}") int thumbnailWidth,
                                 @Value("${marketplace.media.thumbnail.size.height}") int thumbnailHeight) {

        this.mediaExternalClient = mediaExternalClient;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
    }


    public Resource generateThumbnail(MediaLocation mediaLocation) throws ThumbnailGenerationException {
        DownloadedMediaFile downloadedMediaFile = mediaExternalClient.fetchMediaFile(mediaLocation);
        return downloadedMediaFile.consumeFile(this::generateThumbnail);
    }

    public Resource generateThumbnail(Path mediaPath) throws ThumbnailGenerationException {
        try {
            InputStream mediaStream = Files.newInputStream(mediaPath);
            mediaStream = new BufferedInputStream(mediaStream);

            return generateThumbnail(mediaStream);
        }
        catch (IOException e) {
            throw new ThumbnailGenerationException("Error while opening media file", e);
        }
    }

    private Resource generateThumbnail(InputStream mediaStream) throws ThumbnailGenerationException {
        try {
            BufferedImage mediaImage = ImageIO.read(mediaStream);
            BufferedImage thumbImage = resizeImage(mediaImage);

            ByteArrayOutputStream thumbnailBytes = new ByteArrayOutputStream();
            ImageIO.write(thumbImage, THUMBNAIL_FILE_EXTENSION, thumbnailBytes);

            return new ByteArrayResource(thumbnailBytes.toByteArray());

        } catch (IOException e) {
            throw new ThumbnailGenerationException("Error while creating a thumb", e);
        }

    }

    private BufferedImage resizeImage(BufferedImage originalImage) {

        BufferedImage thumbImage = null;
        try {
            thumbImage = Thumbnails.of(originalImage).imageType(BufferedImage.TYPE_INT_RGB).forceSize(thumbnailWidth, thumbnailHeight).asBufferedImage();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Graphics2D g = thumbImage.createGraphics();
        g.drawImage(thumbImage, 0, 0, null);
        g.dispose();

        return thumbImage;
    }

    public String getDefaultThumbnailFilename() {
        return DEFAULT_THUMBNAIL_FILENAME;
    }

}
