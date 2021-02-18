package eu.sshopencloud.marketplace.controllers.media;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaInfo;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaFilesController {

    private final MediaStorageService mediaStorageService;


    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaInfo> uploadMedia(@RequestParam("file") MultipartFile mediaFile) {
        MediaInfo mediaInfo = mediaStorageService.saveCompleteMedia(mediaFile.getResource());
        return ResponseEntity.ok(mediaInfo);
    }

    @PostMapping(path = "/upload-chunk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaInfo> uploadMediaChunk(@RequestParam(name = "mediaId", required = false) UUID mediaId,
                                                      @RequestParam("no") int chunkNo,
                                                      @RequestParam("chunk") MultipartFile mediaChunk) {

        MediaInfo mediaInfo = mediaStorageService.saveMediaChunk(mediaId, mediaChunk.getResource(), chunkNo);
        return ResponseEntity.ok(mediaInfo);
    }

    @PostMapping(path = "/complete-upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaInfo> completeMediaUpload(@RequestParam("mediaId") UUID mediaId) {
        MediaInfo mediaInfo = mediaStorageService.completeMediaUpload(mediaId);
        return ResponseEntity.ok(mediaInfo);
    }

    @PostMapping(path = "/import", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaInfo> importMedia(@RequestBody MediaSource mediaSource) {
        MediaInfo mediaInfo = mediaStorageService.importMedia(mediaSource);
        return ResponseEntity.ok(mediaInfo);
    }
}
