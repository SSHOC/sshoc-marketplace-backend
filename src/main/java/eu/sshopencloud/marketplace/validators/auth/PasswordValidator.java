package eu.sshopencloud.marketplace.validators.auth;

import eu.sshopencloud.marketplace.dto.auth.NewPasswordData;
import eu.sshopencloud.marketplace.model.auth.User;
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
public class PasswordValidator {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public String validate(NewPasswordData newPasswordData, Long userId) throws ValidationException {
        User user = userRepository.getOne(userId);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(newPasswordData, "Password");

        if (StringUtils.isBlank(newPasswordData.getNewPassword()) || StringUtils.isBlank(newPasswordData.getVerifiedPassword())) {
            if (StringUtils.isBlank(newPasswordData.getNewPassword())) {
                errors.rejectValue("newPassword", "field.required", "New password is required.");
            }
            if (StringUtils.isBlank(newPasswordData.getVerifiedPassword())) {
                errors.rejectValue("verifiedPassword", "field.required", "Verified password is required.");
            }
        } else {
            if (!newPasswordData.getNewPassword().equals(newPasswordData.getVerifiedPassword())) {
                errors.rejectValue("verifiedPassword", "field.invalid", "Verified password is different than the new password.");
            }
        }

        if (StringUtils.isBlank(newPasswordData.getCurrentPassword())) {
            errors.rejectValue("currentPassword", "field.required", "Current password is required.");
        } else {
            if (!passwordEncoder.matches(newPasswordData.getCurrentPassword(), user.getPassword())) {
                errors.rejectValue("currentPassword", "field.incorrect", "Current password is incorrect.");
            }
        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return passwordEncoder.encode(newPasswordData.getNewPassword());
        }
    }

}
