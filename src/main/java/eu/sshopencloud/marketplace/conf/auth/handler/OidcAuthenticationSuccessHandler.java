package eu.sshopencloud.marketplace.conf.auth.handler;

import eu.sshopencloud.marketplace.conf.auth.ImplicitGrantTokenProvider;
import eu.sshopencloud.marketplace.conf.auth.JwtTokenProvider;
import eu.sshopencloud.marketplace.conf.auth.UserPrincipal;
import eu.sshopencloud.marketplace.filters.auth.CookieUtils;
import eu.sshopencloud.marketplace.repositories.auth.OAuth2AuthorizationRequestParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private JwtTokenProvider jwtTokenProvider;
    private ImplicitGrantTokenProvider implicitGrantTokenProvider;

    @Autowired
    OidcAuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider, ImplicitGrantTokenProvider implicitGrantTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.implicitGrantTokenProvider = implicitGrantTokenProvider;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal.getIdToken() == null) { //If it is directly with username/pwd
            response.addHeader("Authorization", jwtTokenProvider.createToken(authentication));
        } else {
            String targetUrl = determineTargetUrl(request, response, userPrincipal);
            if (response.isCommitted()) {
                logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
                return;
            }

            clearAuthenticationAttributes(request, response);
            clearAuthorizationRequestCookies(request, response);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }


    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, UserPrincipal userPrincipal) {
        String targetUrl = null;
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