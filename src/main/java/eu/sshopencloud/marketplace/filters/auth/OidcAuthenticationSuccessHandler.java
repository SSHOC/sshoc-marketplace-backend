package eu.sshopencloud.marketplace.filters.auth;

import eu.sshopencloud.marketplace.conf.auth.ImplicitGrantTokenProvider;
import eu.sshopencloud.marketplace.conf.auth.JwtTokenProvider;
import eu.sshopencloud.marketplace.conf.auth.UserPrincipal;
import eu.sshopencloud.marketplace.repositories.auth.OAuth2AuthorizationRequestParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OidcAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    private final ImplicitGrantTokenProvider implicitGrantTokenProvider;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal.getIdToken() == null) {
            // authentication with local username and password
            response.addHeader("Authorization", jwtTokenProvider.createToken(authentication));
        } else {
            log.debug("Success OAuth2 authentication for '" + userPrincipal.getUsername() + "'");
            String targetUrl = determineTargetUrl(request, userPrincipal);

            if (response.isCommitted()) {
                logger.error("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }

            clearAuthenticationAttributes(request, response);
            clearAuthorizationRequestCookies(request, response);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }


    protected String determineTargetUrl(HttpServletRequest request, UserPrincipal userPrincipal) {
        String targetUrl;
        if (userPrincipal.isRegistered()) {
            targetUrl = CookieUtils.getCookie(request, OAuth2AuthorizationRequestParams.SUCCESS_REDIRECT_URL)
                    .orElseThrow(() -> new IllegalArgumentException("Request param " + OAuth2AuthorizationRequestParams.SUCCESS_REDIRECT_URL + " is required and cannot be empty")).getValue();
        } else {
            targetUrl = CookieUtils.getCookie(request, OAuth2AuthorizationRequestParams.REGISTRATION_REDIRECT_URL)
                    .orElseThrow(() -> new IllegalArgumentException("Request param " + OAuth2AuthorizationRequestParams.REGISTRATION_REDIRECT_URL + " is required and cannot be empty")).getValue();
        }
        String token = implicitGrantTokenProvider.createToken(userPrincipal);

        return UriComponentsBuilder.fromUriString(targetUrl).fragment(token).build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
    }

    private void clearAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.REGISTRATION_REDIRECT_URL);
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.SUCCESS_REDIRECT_URL);
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL);
    }

}
