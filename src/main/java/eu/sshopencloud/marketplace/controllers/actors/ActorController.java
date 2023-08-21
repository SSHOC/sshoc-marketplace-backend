package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.ActorHistoryDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorController {

    private final PageCoordsValidator pageCoordsValidator;

    private final ActorService actorService;

    @Operation(summary = "Get list of actors in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedActors> getActors(@RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(actorService.getActors(pageCoordsValidator.validate(page, perpage)));
    }


    @Operation(summary = "Get actor given by id with optional list of items that actor contributes to ")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> getActor(@PathVariable("id") long id,
                                             @RequestParam(value = "items", defaultValue = "false") boolean items) {
        return ResponseEntity.ok(actorService.getActor(id, items));
    }


    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> createActor(
            @Parameter(description = "Created actor object", required = true, schema = @Schema(implementation = ActorCore.class)) @RequestBody ActorCore newActor) {
        return ResponseEntity.ok(actorService.createActor(newActor));
    }


    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> updateActor(@PathVariable("id") long id,
                                                @Parameter(description = "Update actor object with given id", required = true, schema = @Schema(implementation = ActorCore.class)) @RequestBody ActorCore updatedActor) {
        return ResponseEntity.ok(actorService.updateActor(id, updatedActor));
    }


    @Operation(summary = "Delete actor by given id. Force delete can be used by administrators only.")
    @DeleteMapping(path = "/{id}")
    public void deleteActor(
            @PathVariable("id") long id,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
        actorService.deleteActor(id, force);
    }


    @Operation(summary = "Merge of actors", operationId = "mergeActor")
    @PostMapping(path = "/{id}/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> mergeActors(@PathVariable("id") long id, @RequestParam("with") List<Long> with) {

        return ResponseEntity.ok(actorService.mergeActors(id, with));
    }


    @Operation(summary = "History of actor", operationId = "getActorHistory")
    @GetMapping(path = "/{id}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActorHistoryDto>> actorHistory(@PathVariable("id") long id) {
        return ResponseEntity.ok(actorService.getHistory(id));
    }
}
