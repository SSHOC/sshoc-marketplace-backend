package eu.sshopencloud.marketplace.model.auth;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;

public enum UserRole {

    CONTRIBUTOR(Arrays.asList(Authority.CONTRIBUTOR)),

    MODERATOR(Arrays.asList(Authority.CONTRIBUTOR, Authority.MODERATOR)),

    ADMINISTRATOR(Arrays.asList(Authority.CONTRIBUTOR, Authority.MODERATOR, Authority.ADMINISTRATOR));

    private List<Authority> authorities;

    UserRole(List<Authority> authorities) {
        this.authorities = authorities;
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public boolean hasModeratorPrivileges() {
        return authorities.contains(Authority.MODERATOR);
    }
}
