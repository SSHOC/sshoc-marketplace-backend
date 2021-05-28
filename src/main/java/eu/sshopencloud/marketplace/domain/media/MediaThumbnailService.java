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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
    private final int thumbnailMainLength;
    private final int thumbnailSideLength;

    private final int thumbnailWidth= 150;
    private final int thumbnailHeight= 100;


    public MediaThumbnailService(MediaExternalClient mediaExternalClient,
                                 @Value("${marketplace.media.thumbnail.size.main}") int thumbnailMainLength,
                                 @Value("${marketplace.media.thumbnail.size.side}") int thumbnailSideLength) {

        this.mediaExternalClient = mediaExternalClient;
        this.thumbnailMainLength = thumbnailMainLength;
        this.thumbnailSideLength = thumbnailSideLength;
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
            BufferedImage thumbImage;

            if(checkSize(mediaImage.getWidth(), mediaImage.getHeight()))
                thumbImage = mediaImage;
            else thumbImage = resizeImage(mediaImage);

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
            thumbImage = Thumbnails.of(originalImage).imageType(BufferedImage.TYPE_3BYTE_BGR).scale((double) thumbnailWidth /(double) originalImage.getWidth(),(double) thumbnailHeight / (double)  originalImage.getHeight()).asBufferedImage();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Graphics2D g = thumbImage.createGraphics();
        g.drawImage(thumbImage, 0, 0, null);
        g.dispose();

        return thumbImage;
    }

    private boolean checkSize(int width, int height) {
        if (width <= thumbnailWidth && height <= thumbnailHeight) {
            return true;
        }
        else return false;
    }

    private Dimension getScaledDimension(Dimension size) {
        if (size.height > size.width) {
            Dimension target = new Dimension(size.height, size.width);
            Dimension scaled = scaleDimension(target);

            return new Dimension(scaled.height, scaled.width);
        }
        else return scaleDimension(size);
    }

    private Dimension scaleDimension(Dimension size) {
        return new Dimension(thumbnailMainLength, (size.height * thumbnailMainLength) / size.width);
    }

    public String getDefaultThumbnailFilename() {
        return DEFAULT_THUMBNAIL_FILENAME;
    }

}
