package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.auth.*;
import eu.sshopencloud.marketplace.dto.sources.SourceOrder;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final PageCoordsValidator pageCoordsValidator;

    private final UserService userService;

    @Operation(summary = "Get all users in pages")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedUsers> getUsers(@RequestParam(value = "order", required = false) UserOrder order,
                                                   @RequestParam(value = "q", required = false) String q,
                                                   @RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(userService.getUsers(order, q, pageCoordsValidator.validate(page, perpage)));
    }

    @Operation(summary = "Get user by id")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUser(@PathVariable("id") long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "Create new user")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> createUser(@Parameter(
            description = "Created user",
            required = true,
            schema = @Schema(implementation =UserCore.class)) @RequestBody UserCore userCore) {
        return ResponseEntity.ok(userService.createUser(userCore));
    }

    @Operation(summary = "Update password for given user")
    @PutMapping(path = "/{id}/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserPassword(@PathVariable("id") long id,
                                                      @Parameter(
                                                              description = "Update user password",
                                                              required = true,
                                                              schema = @Schema(implementation = NewPasswordData.class)) @RequestBody NewPasswordData newPasswordData) {
        return ResponseEntity.ok(userService.updateUserPassword(id, newPasswordData));
    }

    @Operation(summary = "Update displayed name for given user")
    @PutMapping(path = "/{id}/display-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserDisplayName(@PathVariable("id") long id,
                                                         @Parameter(
                                                                 description = "Update user display name",
                                                                 required = true,
                                                                 schema = @Schema(implementation = UserDisplayNameCore.class)) @RequestBody UserDisplayNameCore displayNameCore) {
        return ResponseEntity.ok(userService.updateUserDisplayName(id, displayNameCore));
    }

    @Operation(summary = "Update user status for given user")
    @PutMapping(path = "/{id}/status/{userStatus}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable("id") long id, @PathVariable("userStatus") UserStatus status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }

    @Operation(summary = "Update user role for given user")
    @PutMapping(path = "/{id}/role/{userRole}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserRole(@PathVariable("id") long id, @PathVariable("userRole") UserRole role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

}
