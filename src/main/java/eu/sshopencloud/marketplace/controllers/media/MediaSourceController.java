package eu.sshopencloud.marketplace.controllers.media;

import eu.sshopencloud.marketplace.domain.media.MediaSourceService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDto;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media-sources")
@RequiredArgsConstructor
public class MediaSourceController {

    private final MediaSourceService mediaSourceService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MediaSourceDto>> getAllMediaSources() {
        return ResponseEntity.ok(mediaSourceService.getAllMediaSources());
    }

    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDto> getMediaSource(@PathVariable("code") String mediaSourceCode) {
        return ResponseEntity.ok(mediaSourceService.getMediaSource(mediaSourceCode));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDto> createMediaSource(@Parameter(
            description = "Created media source",
            required = true,
            schema = @Schema(implementation = MediaSourceCore.class)) @RequestBody MediaSourceCore mediaSourceCore) {
        return ResponseEntity.ok(mediaSourceService.registerMediaSource(mediaSourceCore));
    }

    @PutMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDto> updateMediaSource(@PathVariable("code") String mediaSourceCode,
                                                            @Parameter(
                                                                    description = "Update media source",
                                                                    required = true,
                                                                    schema = @Schema(implementation = MediaSourceCore.class)) @RequestBody MediaSourceCore mediaSourceCore) {

        return ResponseEntity.ok(mediaSourceService.updateMediaSource(mediaSourceCode, mediaSourceCore));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteMediaSource(@PathVariable("code") String mediaSourceCode) {
        mediaSourceService.removeMediaSource(mediaSourceCode);
        return ResponseEntity.ok().build();
    }
}
