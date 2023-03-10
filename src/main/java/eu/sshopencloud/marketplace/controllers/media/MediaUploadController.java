package eu.sshopencloud.marketplace.controllers.media;

import eu.sshopencloud.marketplace.controllers.util.MimeTypeUtils;
import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDownload;
import eu.sshopencloud.marketplace.domain.media.dto.MediaLocation;
import eu.sshopencloud.marketplace.domain.media.dto.MediaUploadInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaUploadController {

    private final MediaStorageService mediaStorageService;

    @Operation(summary = "Download media for given media id")
    @GetMapping(path = "/download/{mediaId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getMediaFile(@PathVariable("mediaId") UUID mediaId) {
        MediaDownload mediaDownload = mediaStorageService.getMediaForDownload(mediaId);
        return serveMediaFile(mediaDownload);
    }

    @Operation(summary = "Get thumbnail for given media id")
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

    @Operation(summary = "Get information about given media id")
    @GetMapping(path = "/info/{mediaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaDetails> getMediaInfo(@PathVariable("mediaId") UUID mediaId) {
        MediaDetails details = mediaStorageService.getMediaDetails(mediaId);
        return ResponseEntity.ok(details);
    }


    @Operation(summary = "Upload full media file")
    @PostMapping(path = "/upload/full", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaDetails> uploadMedia(@RequestParam("file") MultipartFile mediaFile) {
        Optional<MediaType> mediaType = MimeTypeUtils.parseMimeType(mediaFile.getContentType());
        MediaDetails mediaDetails = mediaStorageService.saveCompleteMedia(mediaFile.getResource(), mediaType);
        return ResponseEntity.ok(mediaDetails);
    }


    @Operation(summary = "Upload full media file chunk (part)")
    @PostMapping(path = "/upload/chunk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadInfo> uploadMediaChunk(@RequestParam(name = "mediaId", required = false) Optional<UUID> mediaId,
                                                            @RequestParam("no") int chunkNo,
                                                            @RequestParam("chunk") MultipartFile mediaChunk) {

        Optional<MediaType> mediaType = MimeTypeUtils.parseMimeType(mediaChunk.getContentType());
        MediaUploadInfo uploadInfo = mediaStorageService.saveMediaChunk(mediaId, mediaChunk.getResource(), chunkNo, mediaType);

        return ResponseEntity.ok(uploadInfo);
    }

    @Operation(summary = "Complete uploading chunks for media file")
    @PostMapping(path = "/upload/complete/{mediaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaDetails> completeMediaUpload(@PathVariable("mediaId") UUID mediaId,
                                                            @RequestParam(value = "filename", required = false) Optional<String> filename) {

        MediaDetails mediaDetails = mediaStorageService.completeMediaUpload(mediaId, filename);
        return ResponseEntity.ok(mediaDetails);
    }

    @Operation(summary = "Upload media via link (import)")
    @PostMapping(path = "/upload/import", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaDetails> importMedia(@Parameter(
            description = "Media location",
            required = true,
            schema = @Schema(implementation = MediaLocation.class)) @RequestBody MediaLocation mediaLocation) {
        MediaDetails mediaDetails = mediaStorageService.importMedia(mediaLocation);
        return ResponseEntity.ok(mediaDetails);
    }
}
