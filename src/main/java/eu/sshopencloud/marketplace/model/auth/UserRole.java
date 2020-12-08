package eu.sshopencloud.marketplace.model.auth;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public enum UserRole {
    CONTRIBUTOR(List.of(Authority.CONTRIBUTOR)),
    SYSTEM_CONTRIBUTOR(List.of(Authority.CONTRIBUTOR, Authority.SYSTEM_CONTRIBUTOR)),
    MODERATOR(List.of(Authority.CONTRIBUTOR, Authority.MODERATOR)),
    ADMINISTRATOR(List.of(Authority.CONTRIBUTOR, Authority.MODERATOR, Authority.ADMINISTRATOR));


    private final List<Authority> authorities;


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


    public boolean hasContributorPrivileges() {
        return authorities.contains(Authority.CONTRIBUTOR);
    }

    public boolean hasModeratorPrivileges() {
        return authorities.contains(Authority.MODERATOR);
    }

    public boolean hasAdministratorPrivileges() {
        return authorities.contains(Authority.ADMINISTRATOR);
    }
}
