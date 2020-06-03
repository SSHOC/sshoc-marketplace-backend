package eu.sshopencloud.marketplace.filters.auth;

import eu.sshopencloud.marketplace.repositories.auth.OAuth2AuthorizationRequestParams;
import eu.sshopencloud.marketplace.services.auth.OidcAuthenticationProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OidcAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug("Failure via OAuth2 authentication " + exception.getMessage(), exception);
        String targetUrl = determineTargetUrl(request, exception);
        clearAuthorizationRequestCookies(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, AuthenticationException exception) {
        String targetUrl = CookieUtils.getCookie(request, OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL)
                .orElseThrow(() -> new IllegalArgumentException("Request param " + OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL + " is required and cannot be empty")).getValue();
        String errorCode = "error.unknownError";
        if (exception instanceof OidcAuthenticationProcessingException) {
            errorCode = ((OidcAuthenticationProcessingException)exception).getErrorCode();
        }
        return UriComponentsBuilder.fromUriString(targetUrl).fragment(errorCode).build().toUriString();
    }

    private void clearAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.REGISTRATION_REDIRECT_URL);
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.SUCCESS_REDIRECT_URL);
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL);
    }

}
