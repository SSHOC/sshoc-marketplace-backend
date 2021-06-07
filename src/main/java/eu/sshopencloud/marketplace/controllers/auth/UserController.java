package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.controllers.PageTooLargeException;
import eu.sshopencloud.marketplace.dto.auth.PaginatedUsers;
import eu.sshopencloud.marketplace.dto.auth.UserCore;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.validators.PageCoordsValidator;
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

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedUsers> getUsers(@RequestParam(value = "q", required = false) String q,
                                                   @RequestParam(value = "page", required = false) Integer page,
                                                   @RequestParam(value = "perpage", required = false) Integer perpage)
            throws PageTooLargeException {
        return ResponseEntity.ok(userService.getUsers(q, pageCoordsValidator.validate(page, perpage)));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUser(@PathVariable("id") long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> createUser(@RequestBody UserCore userCore) {
        return ResponseEntity.ok(userService.createUser(userCore));
    }

    @PutMapping(path = "/{id}/status/{userStatus}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable("id") long id, @PathVariable("userStatus") UserStatus status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }

    @PutMapping(path = "/{id}/role/{userRole}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserRole(@PathVariable("id") long id, @PathVariable("userRole") UserRole role) {
        return ResponseEntity.ok(userService.updateUserRole(id, role));
    }

}
