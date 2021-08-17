package eu.sshopencloud.marketplace.controllers.actors;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleCore;
import eu.sshopencloud.marketplace.dto.actors.ActorRoleDto;
import eu.sshopencloud.marketplace.services.actors.ActorRoleService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/actor-roles")
@RequiredArgsConstructor
public class ActorRoleController {

    private final ActorRoleService actorRoleService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActorRoleDto>> getAllActorRoles() {
        return ResponseEntity.ok(actorRoleService.getAllActorRoles());
    }

    @GetMapping(path = "/{roleCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorRoleDto> getActorRole(@PathVariable("roleCode") String code) {
        return ResponseEntity.ok(actorRoleService.getActorRole(code));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorRoleDto> createActorRole(@Parameter(
            description = "Created actor role object",
            required = true,
            schema = @Schema(implementation = ActorRoleCore.class)) @RequestBody ActorRoleCore actorRole) {
        return ResponseEntity.ok(actorRoleService.createActorRole(actorRole));
    }

    @PutMapping(path = "/{roleCode}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ActorRoleDto> updateActorRole(@PathVariable("roleCode") String code,
                                                        @Parameter(
                                                                description = "Update actor role object",
                                                                required = true,
                                                                schema = @Schema(implementation = ActorRoleCore.class))  @RequestBody ActorRoleCore actorRole) {

        return ResponseEntity.ok(actorRoleService.updateActorRole(code, actorRole));
    }

    @DeleteMapping("/{roleCode}")
    public ResponseEntity<Void> deleteActorRole(@PathVariable("roleCode") String code) {
        actorRoleService.deleteActorRole(code);
        return ResponseEntity.ok().build();
    }
}
