package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.auth.OAuthRegistrationData;
import eu.sshopencloud.marketplace.services.auth.OAuth2RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuth2RegistrationController {

    private final OAuth2RegistrationService oAuth2RegistrationService;

    @PutMapping(path = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> registerOAuth2User(@RequestBody OAuthRegistrationData oAuthRegistrationData) {
        return ResponseEntity.ok(oAuth2RegistrationService.registerOAuth2User(oAuthRegistrationData));
    }

}
