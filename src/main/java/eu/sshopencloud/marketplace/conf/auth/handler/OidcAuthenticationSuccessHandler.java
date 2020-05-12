package eu.sshopencloud.marketplace.conf.auth.handler;

import eu.sshopencloud.marketplace.conf.auth.TokenProvider;
import eu.sshopencloud.marketplace.conf.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OidcAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private TokenProvider tokenProvider;

    @Autowired
    OidcAuthenticationSuccessHandler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Value("${marketplace.security.oauth2.redirectAfterLogin}")
    private String redirectAfterLogin;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if(((UserPrincipal)authentication.getPrincipal()).getIdToken() == null) { //If it is directly with username/pwd
            response.addHeader("Authorization", tokenProvider.createToken(authentication));
        } else {
            String targetUrl = determineTargetUrl(request, response, authentication);

            if (response.isCommitted()) {
                logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }

            clearAuthenticationAttributes(request, response);
            response.addHeader("Authorization", tokenProvider.createToken(authentication));
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//        String token = tokenProvider.createToken(authentication);
//        log.debug("Token created for frontend: " + token);

        return UriComponentsBuilder.fromUriString(redirectAfterLogin)
//                .queryParam("token", token)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
    }
}