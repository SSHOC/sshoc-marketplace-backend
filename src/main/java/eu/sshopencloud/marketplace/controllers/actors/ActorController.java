package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActorController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    private final ActorService actorService;

    @GetMapping(path = "/actors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Actor>> getActors(@RequestParam(value = "q", required = false) String q) {
        List<Actor> actors = actorService.getActors(q, defualtPerpage);
        return ResponseEntity.ok(actors);
    }

    @GetMapping(path = "/actors/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Actor> getActor(@PathVariable("id") long id) {
        Actor actor = actorService.getActor(id);
        return ResponseEntity.ok(actor);
    }

    @PostMapping(path = "/actors", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Actor> createActor(@RequestBody ActorCore newActor) {
        return ResponseEntity.ok(actorService.createActor(newActor));
    }

    @PutMapping(path = "/actors/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Actor> updateActor(@PathVariable("id") long id, @RequestBody ActorCore updatedActor) {
        return ResponseEntity.ok(actorService.updateActor(id, updatedActor));
    }

    @DeleteMapping("/actors/{id}")
    public void deleteActor(@PathVariable("id") long id) {
        actorService.deleteActor(id);
    }

}
