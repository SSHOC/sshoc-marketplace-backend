package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "actors")
@Data
@NoArgsConstructor
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_generator")
    @SequenceGenerator(name = "actor_generator", sequenceName = "actors_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    @Column(nullable = false)
    private String name;

    @Basic
    @Column(nullable = true)
    private String website;

    @Basic
    @Column(nullable = true)
    private String email;


    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
    @JoinTable(name = "actors_affiliations", joinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "affiliation_id", referencedColumnName = "id"))
    @OrderColumn(name = "ord")
    private List<Actor> affiliations;

    @OneToMany(mappedBy = "actor", cascade = { CascadeType.REFRESH })
    @JsonIgnore
    private List<ItemContributor> contributorTo;

}
