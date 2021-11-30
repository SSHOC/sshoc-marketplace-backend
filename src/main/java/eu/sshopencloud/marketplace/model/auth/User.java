package eu.sshopencloud.marketplace.model.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(of = { "id", "username", "role" })
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name = "user_generator", sequenceName = "users_id_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String displayName;

    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private ZonedDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String provider;

    @Column(nullable = true)
    @Nullable
    private String tokenKey;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean config;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String preferences;


    public boolean isContributor() {
        return role.hasContributorPrivileges();
    }

    public boolean isModerator() {
        return role.hasModeratorPrivileges();
    }

    public boolean isAdministrator() {
        return role.hasAdministratorPrivileges();
    }

    public boolean isSystemContributor() {
        return role.equals(UserRole.SYSTEM_CONTRIBUTOR);
    }

    public boolean isSystemModerator() {
        return role.equals(UserRole.SYSTEM_MODERATOR);
    }

}
