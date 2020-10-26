package eu.sshopencloud.marketplace.controllers.auth;

import eu.sshopencloud.marketplace.dto.auth.LoginData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class FakeAuthController {

    @Operation(summary = "Sign into the system")
    @PostMapping(value = "/api/auth/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    void signIn(@RequestBody LoginData loginData){}

    @Operation(summary = "Sign into the system using oauth2")
    @GetMapping(value = "/oauth2/authorize/eosc")
    void oauth2(@RequestParam(value = "success-redirect-url") String successRedirectUrl,
                @RequestParam(value = "failure-redirect-url") String failureRedirectUrl,
                @RequestParam(value = "registration-redirect-url") String registrationRedirectUrl){}

}
