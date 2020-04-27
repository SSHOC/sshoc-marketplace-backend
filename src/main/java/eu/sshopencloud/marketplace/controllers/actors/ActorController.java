package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorDto;
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
    public ResponseEntity<List<ActorDto>> getActors(@RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(actorService.getActors(q, defualtPerpage));
    }

    @GetMapping(path = "/actors/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> getActor(@PathVariable("id") long id) {
        return ResponseEntity.ok(actorService.getActor(id));
    }

    @PostMapping(path = "/actors", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> createActor(@RequestBody ActorCore newActor) {
        return ResponseEntity.ok(actorService.createActor(newActor));
    }

    @PutMapping(path = "/actors/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorDto> updateActor(@PathVariable("id") long id, @RequestBody ActorCore updatedActor) {
        return ResponseEntity.ok(actorService.updateActor(id, updatedActor));
    }

    @DeleteMapping("/actors/{id}")
    public void deleteActor(@PathVariable("id") long id) {
        actorService.deleteActor(id);
    }

}
