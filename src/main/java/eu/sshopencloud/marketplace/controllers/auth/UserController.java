package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.services.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    @Value("${marketplace.pagination.default-perpage}")
    private Integer defualtPerpage;

    private final UserService userService;

    @GetMapping(path = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(userService.getUsers(q, defualtPerpage));
    }

    @GetMapping(path = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUser(@PathVariable("id") long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

}
