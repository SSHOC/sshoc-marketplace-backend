package eu.sshopencloud.marketplace.controllers.media;

import eu.sshopencloud.marketplace.domain.media.MediaSourceCrudService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media-sources")
@RequiredArgsConstructor
public class MediaSourceController {

    private final MediaSourceCrudService mediaSourceService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MediaSourceDetails>> getAllMediaSources() {
        return ResponseEntity.ok(mediaSourceService.getAllMediaSources());
    }

    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDetails> getMediaSource(@PathVariable("code") String mediaSourceCode) {
        return ResponseEntity.ok(mediaSourceService.getMediaSource(mediaSourceCode));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDetails> postMediaSource(@RequestBody MediaSourceCore mediaSourceCore) {
        return ResponseEntity.ok(mediaSourceService.registerMediaSource(mediaSourceCore));
    }

    @PutMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDetails> putMediaSource(@PathVariable("code") String mediaSourceCode,
                                                             @RequestBody MediaSourceCore mediaSourceCore) {

        return ResponseEntity.ok(mediaSourceService.updateMediaSource(mediaSourceCode, mediaSourceCore));
    }
}
