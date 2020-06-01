package eu.sshopencloud.marketplace.model.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @SequenceGenerator(name = "user_generator", sequenceName = "users_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    @Column(nullable = false, unique = true)
    private String username;

    @Basic
    @Column
    private String displayName;

    @Basic
    @Column(nullable = true)
    private String password;

    @Basic()
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean enabled;

    @Basic()
    @Column(nullable = true)
    private ZonedDateTime registrationDate;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserRole role;

    @Basic
    @Column(nullable = true)
    private String provider;

    @Basic
    @Column(nullable = true)
    private String tokenKey;

    @Basic
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Basic(fetch = FetchType.EAGER, optional = false)
    private String preferences;

}
