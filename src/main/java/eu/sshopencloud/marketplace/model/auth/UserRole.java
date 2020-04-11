package eu.sshopencloud.marketplace.model.auth;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {

    CONTRIBUTOR,

    MODERATOR,

    ADMINISTRATOR;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public String getAuthority() {
        return name();
    }

}
