package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
import eu.sshopencloud.marketplace.dto.actors.PaginatedActors;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorController {

    private final PageCoordsValidator pageCoordsValidator;

    private final ActorService actorService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedActors> getActors(@RequestParam(value = "q", required = false) String q,
                                                     @RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(actorService.getActors(q, pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> getActor(@PathVariable("id") long id) {
        return ResponseEntity.ok(actorService.getActor(id));
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> createActor(@RequestBody ActorCore newActor) {
        return ResponseEntity.ok(actorService.createActor(newActor));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> updateActor(@PathVariable("id") long id, @RequestBody ActorCore updatedActor) {
        return ResponseEntity.ok(actorService.updateActor(id, updatedActor));
    }

    @DeleteMapping(path = "/{id}")
    public void deleteActor(@PathVariable("id") long id) {
        actorService.deleteActor(id);
    }

}
