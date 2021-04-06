package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.auth.OAuthRegistrationData;
import eu.sshopencloud.marketplace.mappers.auth.UserMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.validators.auth.OAuth2RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuth2RegistrationService {

    private final UserRepository userRepository;

    private final OAuth2RegistrationValidator oAuth2RegistrationValidator;


    public UserDto registerOAuth2User(OAuthRegistrationData oAuthRegistrationData) {
        User loggedInUser = LoggedInUserHolder.getLoggedInUser();
        User user = oAuth2RegistrationValidator.validate(oAuthRegistrationData, loggedInUser.getId());
        user.setStatus(UserStatus.ENABLED);
        user.setRole(UserRole.CONTRIBUTOR);
        user.setRegistrationDate(ZonedDateTime.now());
        userRepository.save(user);

        return UserMapper.INSTANCE.toDto(user);
    }

}
