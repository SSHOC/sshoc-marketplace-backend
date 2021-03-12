package eu.sshopencloud.marketplace.domain.media;

import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
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
    private final int thumbnailMainLength;
    private final int thumbnailSideLength;


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
            BufferedImage thumbImage = resizeImage(mediaImage);

            ByteArrayOutputStream thumbnailBytes = new ByteArrayOutputStream();
            ImageIO.write(thumbImage, THUMBNAIL_FILE_EXTENSION, thumbnailBytes);

            return new InputStreamResource(new ByteArrayInputStream(thumbnailBytes.toByteArray()));
        }
        catch (IOException e) {
            throw new ThumbnailGenerationException("Error while creating a thumb", e);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        Dimension originalSize = new Dimension(originalImage.getWidth(), originalImage.getHeight());
        Dimension thumbSize = getScaledDimension(originalSize);

        Image thumbImage = originalImage.getScaledInstance(thumbSize.width, thumbSize.height, Image.SCALE_SMOOTH);

        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        BufferedImage resizedImage = new BufferedImage(thumbSize.width, thumbSize.height, type);

        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(thumbImage, 0, 0, null);
        g.dispose();

        return resizedImage;
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
