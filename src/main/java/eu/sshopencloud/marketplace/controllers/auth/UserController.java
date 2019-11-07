package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.model.auth.User;
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
    public ResponseEntity<List<User>> getUsers(@RequestParam(value = "q", required = false) String q) {
        List<User> users = userService.getUsers(q, defualtPerpage);
        return ResponseEntity.ok(users);
    }

    @GetMapping(path = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUser(@PathVariable("id") long id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

}
