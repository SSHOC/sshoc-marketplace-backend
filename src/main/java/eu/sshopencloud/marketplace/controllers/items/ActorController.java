package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.model.items.Actor;
import eu.sshopencloud.marketplace.services.items.ActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;

    @GetMapping("/actors")
    public ResponseEntity<List<Actor>> getAllActors() {
        List<Actor> actors = actorService.getAllActors();
        return ResponseEntity.ok(actors);
    }

}
