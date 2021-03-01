package eu.sshopencloud.marketplace.controllers.media;

import eu.sshopencloud.marketplace.controllers.util.MimeTypeUtils;
import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDownload;
import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSource;
import eu.sshopencloud.marketplace.domain.media.dto.MediaUploadInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaUploadController {

    private final MediaStorageService mediaStorageService;


    @GetMapping(path = "/download/{mediaId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getMediaFile(@PathVariable("mediaId") UUID mediaId) {
        MediaDownload mediaDownload = mediaStorageService.getMediaForDownload(mediaId);
        return serveMediaFile(mediaDownload);
    }

    @GetMapping(path = "/thumbnail/{mediaId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getMediaThumbnail(@PathVariable("mediaId") UUID mediaId) {
        MediaDownload thumbnailDownload = mediaStorageService.getThumbnailForDownload(mediaId);
        return serveMediaFile(thumbnailDownload);
    }

    private ResponseEntity<Resource> serveMediaFile(MediaDownload mediaDownload) {
        MediaType contentType = (mediaDownload.getMimeType() != null) ?
                mediaDownload.getMimeType() : MediaType.APPLICATION_OCTET_STREAM;

        HttpHeaders headers = new HttpHeaders();

        if (mediaDownload.getFilename() != null) {
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename(mediaDownload.getFilename())
                            .build()
            );
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(mediaDownload.getContentLength())
                .contentType(contentType)
                .body(mediaDownload.getMediaFile());
    }

    @PostMapping(path = "/upload/full", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaInfo> uploadMedia(@RequestParam("file") MultipartFile mediaFile) {
        Optional<MediaType> mediaType = MimeTypeUtils.parseMimeType(mediaFile.getContentType());
        MediaInfo mediaInfo = mediaStorageService.saveCompleteMedia(mediaFile.getResource(), mediaType);

        return ResponseEntity.ok(mediaInfo);
    }

    @PostMapping(path = "/upload/chunk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadInfo> uploadMediaChunk(@RequestParam(name = "mediaId", required = false) Optional<UUID> mediaId,
                                                            @RequestParam("no") int chunkNo,
                                                            @RequestParam("chunk") MultipartFile mediaChunk) {

        Optional<MediaType> mediaType = MimeTypeUtils.parseMimeType(mediaChunk.getContentType());
        MediaUploadInfo uploadInfo= mediaStorageService.saveMediaChunk(mediaId, mediaChunk.getResource(), chunkNo, mediaType);

        return ResponseEntity.ok(uploadInfo);
    }

    @PostMapping(path = "/upload/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaInfo> completeMediaUpload(@RequestParam("mediaId") UUID mediaId) {
        MediaInfo mediaInfo = mediaStorageService.completeMediaUpload(mediaId);
        return ResponseEntity.ok(mediaInfo);
    }

    @PostMapping(path = "/upload/import", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaInfo> importMedia(@RequestBody MediaSource mediaSource) {
        MediaInfo mediaInfo = mediaStorageService.importMedia(mediaSource);
        return ResponseEntity.ok(mediaInfo);
    }
}
