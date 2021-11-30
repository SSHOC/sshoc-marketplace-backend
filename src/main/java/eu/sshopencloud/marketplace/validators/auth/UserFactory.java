package eu.sshopencloud.marketplace.validators.auth;

import eu.sshopencloud.marketplace.dto.auth.UserCore;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import eu.sshopencloud.marketplace.repositories.auth.UserRepository;
import eu.sshopencloud.marketplace.validators.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserFactory {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User create(UserCore userCore) throws ValidationException {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(userCore, "User");

        User user = userRepository.findByUsername(userCore.getUsername());

        if (user != null) {
            errors.rejectValue("username", "field.alreadyExists", "User already exists.");
        } else {
            user = userRepository.findByEmail(userCore.getEmail());
            if (user != null) {
                errors.rejectValue("email", "field.alreadyExists", "User already exists.");
            } else {
                user = new User();
                if (StringUtils.isBlank(userCore.getUsername())) {
                    errors.rejectValue("username", "field.required", "Username is required.");
                } else {
                    user.setUsername(userCore.getUsername());
                }

                if (StringUtils.isBlank(userCore.getDisplayName())) {
                    errors.rejectValue("displayName", "field.required", "Display name is required.");
                } else {
                    user.setDisplayName(userCore.getDisplayName());
                }

                if (StringUtils.isBlank(userCore.getPassword())) {
                    errors.rejectValue("password", "field.required", "Password is required.");
                } else {
                    user.setPassword(passwordEncoder.encode(userCore.getPassword()));
                }

                if (userCore.getRole() != null) {
                    user.setRole(userCore.getRole());
                } else {
                    user.setRole(UserRole.CONTRIBUTOR);
                }

                if (StringUtils.isBlank(userCore.getEmail())) {
                    errors.rejectValue("email", "field.required", "Email is required.");
                } else {
                    if (EmailValidator.isValid(userCore.getEmail())) {
                        user.setEmail(userCore.getEmail());
                    } else {
                        errors.rejectValue("email", "field.invalid", "Email is malformed.");
                    }
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
