package eu.sshopencloud.marketplace.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

    @Column(nullable = false, columnDefinition = "TEXT")
    @Basic(fetch = FetchType.EAGER, optional = false)
    @JsonIgnore
    private String preferences;

}
