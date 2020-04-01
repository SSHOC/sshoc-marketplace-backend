package eu.sshopencloud.marketplace.model.actors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "actors")
@Data
@ToString(exclude = {"contributorTo"})
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

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH})
    @JoinTable(name = "actors_affiliations", joinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "actors_affiliations_actor_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "affiliation_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "actors_affiliations_affiliation_id_fk")))
    @OrderColumn(name = "ord")
    private List<Actor> affiliations;

    @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JsonIgnore
    private List<ItemContributor> contributorTo;

}
