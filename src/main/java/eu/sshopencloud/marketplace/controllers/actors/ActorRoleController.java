package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.model.actors.ActorRole;
import eu.sshopencloud.marketplace.services.actors.ActorRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActorRoleController {

    private final ActorRoleService actorRoleService;

    @GetMapping(path = "/actor-roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActorRole>> getAllActorRoles() {
        List<ActorRole> actorRoles = actorRoleService.getAllActorRoles();
        return ResponseEntity.ok(actorRoles);
    }

}
