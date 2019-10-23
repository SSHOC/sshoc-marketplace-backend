package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.services.actors.ActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;

    @GetMapping(path = "/actors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Actor>> getActors(@RequestParam(value = "q", required = false) String q) {
        List<Actor> actors = actorService.getActors(q);
        return ResponseEntity.ok(actors);
    }

}
