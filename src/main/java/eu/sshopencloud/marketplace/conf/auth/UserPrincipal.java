package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.auth.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.*;

@Slf4j
public class UserPrincipal implements UserDetails, OidcUser {

    private OidcUser oidcUser;
    private Long id;
    private String email;
    private String username;
    private String password;
    private UserStatus status;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private boolean registered;
    private String tokenKey;

    public UserPrincipal(Long id, String email, String username, String password, UserStatus status, String tokenKey, boolean registered, OidcUser oidcUser) {
        this.oidcUser = oidcUser;
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = oidcUser.getAuthorities();
        this.registered = registered;
        this.tokenKey = tokenKey;
    }

    public UserPrincipal(Long id, String email, String username, String password, UserStatus status, String tokenKey, boolean registered,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
        this.registered = registered;
        this.tokenKey = tokenKey;
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                user.getTokenKey(),
                user.getRegistrationDate() != null,
                user.getRole() != null ? user.getRole().getAuthorities() : Collections.emptyList()
        );
    }

    public static UserPrincipal create(User user, OidcUser oidcUser) {
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                user.getTokenKey(),
                user.getRegistrationDate() != null,
                oidcUser
        );
        userPrincipal.setAttributes(oidcUser.getAttributes());
        return userPrincipal;
    }


    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !status.equals(UserStatus.LOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status.equals(UserStatus.ENABLED);
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(username);
    }

    @Override
    public Map<String, Object> getClaims() {
        if(oidcUser != null)
            return oidcUser.getClaims();
        return null;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        if(oidcUser != null)
            return oidcUser.getUserInfo();
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        if(oidcUser != null)
            return oidcUser.getIdToken();
        return null;
    }

}