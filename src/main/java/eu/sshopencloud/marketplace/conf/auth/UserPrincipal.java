package eu.sshopencloud.marketplace.conf.auth;

import eu.sshopencloud.marketplace.model.auth.Authority;
import eu.sshopencloud.marketplace.model.auth.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, String email, String username, String password, OidcUser oidcUser) {
        this.oidcUser = oidcUser;
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = oidcUser.getAuthorities();
    }
    public UserPrincipal(Long id, String email, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for(Authority authority : user.getRole().getAuthorities()) {
            authorities.add(new SimpleGrantedAuthority(authority.getAuthority()));
        }
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    public static UserPrincipal create(User user, OidcUser oidcUser) {
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                oidcUser
        );
        userPrincipal.setAttributes(oidcUser.getAttributes());
        return userPrincipal;
    }
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
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