package eu.sshopencloud.marketplace.controllers.items;

import eu.sshopencloud.marketplace.model.items.ActorRole;
import eu.sshopencloud.marketplace.services.items.ActorRoleService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/actor-roles")
    public ResponseEntity<List<ActorRole>> getAllActorRoles() {
        List<ActorRole> actorRoles = actorRoleService.getAllActorRoles();
        return ResponseEntity.ok(actorRoles);
    }

}
