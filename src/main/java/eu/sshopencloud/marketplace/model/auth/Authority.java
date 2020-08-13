package eu.sshopencloud.marketplace.model.auth;

import org.springframework.security.core.GrantedAuthority;

public enum Authority implements GrantedAuthority {

    CONTRIBUTOR,

    MODERATOR,

    ADMINISTRATOR;

    @Override
    public String getAuthority() {
        return name();
    }

}