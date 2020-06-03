package eu.sshopencloud.marketplace.validators.auth;

import eu.sshopencloud.marketplace.dto.auth.OAuthRegistrationData;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OAuth2RegistrationValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    private final UserRepository userRepository;


    public User validate(OAuthRegistrationData oAuthRegistrationData, Long userId) throws ValidationException {
        User user = userRepository.getOne(userId);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(oAuthRegistrationData, "User");

        if (user.getRegistrationDate() != null) {
            errors.rejectValue("id", "field.isAlreadyRegistered", "User is already registered.");
        } else {
            if (!oAuthRegistrationData.isAcceptedRegulations()) {
                errors.rejectValue("acceptedRegulations", "field.required", "You have to accept regulations.");
            }

            if (StringUtils.isBlank(oAuthRegistrationData.getDisplayName())) {
                errors.rejectValue("displayName", "field.required", "Display name is required.");
            } else {
                user.setDisplayName(oAuthRegistrationData.getDisplayName());
            }

            if (StringUtils.isBlank(oAuthRegistrationData.getEmail())) {
                errors.rejectValue("email", "field.required", "Email is required.");
            } else {
                if (EMAIL_PATTERN.matcher(oAuthRegistrationData.getEmail()).matches()) {
                    user.setEmail(oAuthRegistrationData.getEmail());
                } else {
                    errors.rejectValue("email", "field.invalid", "Email is malformed.");
                }
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return user;
        }
    }

}
