package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceCore;
import eu.sshopencloud.marketplace.dto.actors.ActorSourceDto;
import eu.sshopencloud.marketplace.services.actors.ActorSourceService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/actor-sources")
@RequiredArgsConstructor
public class ActorSourceController {

    private final ActorSourceService actorSourceService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActorSourceDto>> getAllActorSources() {
        return ResponseEntity.ok(actorSourceService.getAllActorSources());
    }

    @GetMapping(path = "/{sourceCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorSourceDto> getActorSource(@PathVariable("sourceCode") String code) {
        return ResponseEntity.ok(actorSourceService.getActorSource(code));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorSourceDto> createActorSource(@Parameter(
            description = "Created actor source object",
            required = true,
            schema = @Schema(implementation = ActorSourceCore.class)) @RequestBody ActorSourceCore actorSource) {
        return ResponseEntity.ok(actorSourceService.createActorSource(actorSource));
    }

    @PutMapping(path = "/{sourceCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorSourceDto> updateActorSource(@PathVariable("sourceCode") String code,
                                                            @Parameter(
                                                                    description = "Update actor source object",
                                                                    required = true,
                                                                    schema = @Schema(implementation = ActorSourceCore.class))@RequestBody ActorSourceCore actorSource) {

        return ResponseEntity.ok(actorSourceService.updateActorSource(code, actorSource));
    }

    @DeleteMapping("/{sourceCode}")
    public ResponseEntity<Void> deleteActorSource(@PathVariable("sourceCode") String code) {
        actorSourceService.deleteActorSource(code);
        return ResponseEntity.ok().build();
    }
}
