package eu.sshopencloud.marketplace.model.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

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
    @GenericGenerator(
            name = "user_generator", strategy = "eu.sshopencloud.marketplace.conf.jpa.KnownIdOrSequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "users_id_seq"),
                    @Parameter(name = "increment_size", value = "50"),
            }
    )
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
