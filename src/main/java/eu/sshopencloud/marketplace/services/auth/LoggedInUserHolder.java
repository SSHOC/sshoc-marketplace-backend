package eu.sshopencloud.marketplace.services.auth;

import eu.sshopencloud.marketplace.conf.auth.UserPrincipal;
import eu.sshopencloud.marketplace.model.auth.Authority;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;


@UtilityClass
public class LoggedInUserHolder {

    @SuppressWarnings("unchecked")
    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            User user = new User();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            user.setId(userPrincipal.getId());
            user.setUsername(userPrincipal.getUsername());
            user.setStatus(userPrincipal.getStatus());
            user.setRole(findRoleByAuthorities((Collection<Authority>) authentication.getAuthorities()));
            return user;
        }
    }

    private UserRole findRoleByAuthorities(Collection<Authority> authorities) {
        if (authorities.contains(Authority.ADMINISTRATOR)) {
            return UserRole.ADMINISTRATOR;
        }
        if (authorities.contains(Authority.SYSTEM_MODERATOR)) {
            return UserRole.SYSTEM_MODERATOR;
        }
        if (authorities.contains(Authority.MODERATOR)) {
            return UserRole.MODERATOR;
        }
        if (authorities.contains(Authority.SYSTEM_CONTRIBUTOR)) {
            return UserRole.SYSTEM_CONTRIBUTOR;
        }
        if (authorities.contains(Authority.CONTRIBUTOR)) {
            return UserRole.CONTRIBUTOR;
        }
        return null;
    }

}
