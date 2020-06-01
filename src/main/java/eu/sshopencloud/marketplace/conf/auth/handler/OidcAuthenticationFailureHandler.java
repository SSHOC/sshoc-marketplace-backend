package eu.sshopencloud.marketplace.conf.auth.handler;

import eu.sshopencloud.marketplace.filters.auth.CookieUtils;
import eu.sshopencloud.marketplace.repositories.auth.OAuth2AuthorizationRequestParams;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OidcAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String targetUrl = CookieUtils.getCookie(request, OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL)
                .orElseThrow(() -> new IllegalArgumentException("Request param " + OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL + " is required and cannot be empty")).getValue();
        clearAuthorizationRequestCookies(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void clearAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.REGISTRATION_REDIRECT_URL);
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.SUCCESS_REDIRECT_URL);
        CookieUtils.deleteCookie(request, response, OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL);
    }

}
