package eu.sshopencloud.marketplace.model.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_generator")
    @GenericGenerator(name = "user_generator", strategy = "eu.sshopencloud.marketplace.conf.jpa.KnownIdOrSequenceStyleGenerator", parameters = {
            @Parameter(name = "sequence_name", value = "users_id_seq"),
            @Parameter(name = "increment_size", value = "50"),
    })
    private Long id;

    @Basic
    @Column(nullable = false, unique = true)
    private String username;

    @Basic
    @Column(nullable = true)
    private String password;

    @Basic()
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean enabled;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private UserRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Basic(fetch = FetchType.EAGER, optional = false)
    private String preferences;

}

