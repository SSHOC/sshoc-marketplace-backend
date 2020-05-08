package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.services.actors.ActorRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/actor-roles")
@RequiredArgsConstructor
public class ActorRoleController {

    private final ActorRoleService actorRoleService;

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActorRoleDto>> getAllActorRoles() {
        return ResponseEntity.ok(actorRoleService.getAllActorRoles());
    }

}
