package eu.sshopencloud.marketplace.controllers.media;

import eu.sshopencloud.marketplace.domain.media.MediaSourceService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceCore;
import eu.sshopencloud.marketplace.domain.media.dto.MediaSourceDto;
import io.swagger.v3.oas.annotations.Operation;
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


    @Operation(summary = "Getting list of all media sources")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MediaSourceDto>> getAllMediaSources() {
        return ResponseEntity.ok(mediaSourceService.getAllMediaSources());
    }

    @Operation(summary = "Getting single media source for given media source code")
    @GetMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDto> getMediaSource(@PathVariable("code") String mediaSourceCode) {
        return ResponseEntity.ok(mediaSourceService.getMediaSource(mediaSourceCode));
    }

    @Operation(summary = "Creating media source")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDto> createMediaSource(@Parameter(
            description = "Created media source",
            required = true,
            schema = @Schema(implementation = MediaSourceCore.class)) @RequestBody MediaSourceCore mediaSourceCore) {
        return ResponseEntity.ok(mediaSourceService.registerMediaSource(mediaSourceCore));
    }

    @Operation(summary = "Updating media source for given media source code")
    @PutMapping(path = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaSourceDto> updateMediaSource(@PathVariable("code") String mediaSourceCode,
                                                            @Parameter(
                                                                    description = "Update media source",
                                                                    required = true,
                                                                    schema = @Schema(implementation = MediaSourceCore.class)) @RequestBody MediaSourceCore mediaSourceCore) {

        return ResponseEntity.ok(mediaSourceService.updateMediaSource(mediaSourceCode, mediaSourceCore));
    }

    @Operation(summary = "Deleting media source for given media source code")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteMediaSource(@PathVariable("code") String mediaSourceCode) {
        mediaSourceService.removeMediaSource(mediaSourceCode);
        return ResponseEntity.ok().build();
    }
}
