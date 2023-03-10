package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.dto.actors.ActorCore;
import eu.sshopencloud.marketplace.dto.auth.ImplicitGrantTokenData;
import eu.sshopencloud.marketplace.dto.auth.OAuthRegistrationDto;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.services.auth.InvalidTokenException;
import eu.sshopencloud.marketplace.services.auth.TokenService;
import eu.sshopencloud.marketplace.services.auth.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    private final UserService userService;

    @GetMapping(path = "/auth/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getLoggedInUser() {
        return ResponseEntity.ok(userService.getLoggedInUser());
    }

    @PutMapping(path = "/oauth/token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OAuthRegistrationDto> validateImplicitGrantToken(@Parameter(
            description = "Authorization",
            required = true,
            schema = @Schema(implementation = ImplicitGrantTokenData.class))@RequestBody ImplicitGrantTokenData implicitGrantTokenData) throws InvalidTokenException {
        String jwtToken = tokenService.validateImplicitGrantToken(implicitGrantTokenData.getToken());
        if (implicitGrantTokenData.isRegistration()) {
            return ResponseEntity.ok().header("Authorization", jwtToken).body(userService.getOAuthRegistration(implicitGrantTokenData.getToken()));
        } else {
            return ResponseEntity.ok().header("Authorization", jwtToken).build();
        }
    }

}
