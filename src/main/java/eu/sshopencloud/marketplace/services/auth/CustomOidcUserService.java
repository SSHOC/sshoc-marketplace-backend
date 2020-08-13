package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.conf.auth.OAuth2UserInfo;
import eu.sshopencloud.marketplace.conf.auth.UserPrincipal;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        try {
            return processOidcUser(userRequest, oidcUser);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) throws OidcAuthenticationProcessingException {
        OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo(oidcUser.getAttributes());

        User user = userRepository.findByUsername(oAuth2UserInfo.getId());
        if (user != null) {
            if(!user.getProvider().equals(userRequest.getClientRegistration().getRegistrationId())) {
                throw new OidcAuthenticationProcessingException("error.otherProvider." +  user.getProvider(),
                        "Looks like you're signed up with " + user.getProvider() + " account. Please use your " + user.getProvider() + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(userRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oidcUser);
    }

    private User registerNewUser(OidcUserRequest userRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setProvider(userRequest.getClientRegistration().getRegistrationId());
        user.setUsername(oAuth2UserInfo.getId());
        user.setDisplayName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setTokenKey(UUID.randomUUID().toString());
        user.setEnabled(false);
        user.setPreferences("{}");
//        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setTokenKey(UUID.randomUUID().toString());
        return userRepository.save(existingUser);
    }

}