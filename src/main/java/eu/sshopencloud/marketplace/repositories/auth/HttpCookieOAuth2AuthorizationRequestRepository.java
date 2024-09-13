package eu.sshopencloud.marketplace.repositories.auth;


import eu.sshopencloud.marketplace.filters.auth.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final static int cookieExpirationSec = 600;

    private final HttpSessionOAuth2AuthorizationRequestRepository httpSessionOAuth2AuthorizationRequestRepository;

    public HttpCookieOAuth2AuthorizationRequestRepository() {
        httpSessionOAuth2AuthorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return httpSessionOAuth2AuthorizationRequestRepository.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        httpSessionOAuth2AuthorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        saveParamInCookie(request, response, OAuth2AuthorizationRequestParams.REGISTRATION_REDIRECT_URL);
        saveParamInCookie(request, response, OAuth2AuthorizationRequestParams.SUCCESS_REDIRECT_URL);
        saveParamInCookie(request, response, OAuth2AuthorizationRequestParams.FAILURE_REDIRECT_URL);
    }

    private void saveParamInCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        String value = request.getParameter(name);
        if (StringUtils.isNotBlank(value)) {
            CookieUtils.addCookie(response, name, value, cookieExpirationSec);
        } else {
            throw new IllegalArgumentException("Request param " + name + " is required and cannot be empty");
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return httpSessionOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
    }

}
