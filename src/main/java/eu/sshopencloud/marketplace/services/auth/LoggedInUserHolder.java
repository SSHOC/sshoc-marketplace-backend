package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@UtilityClass
public class LoggedInUserHolder {

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            User user = new User();
            user.setUsername(authentication.getName());
            return user;
        }
    }

}
