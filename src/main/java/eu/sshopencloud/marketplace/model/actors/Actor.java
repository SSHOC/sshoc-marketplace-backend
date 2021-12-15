package eu.sshopencloud.marketplace.model.actors;

import com.google.gson.annotations.Expose;
import eu.sshopencloud.marketplace.model.items.ItemContributor;
import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "actors")
@Data
@ToString(exclude = { "contributorTo", "history" })
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_generator")
    @SequenceGenerator(name = "actor_generator", sequenceName = "actors_id_seq", allocationSize = 50)
    @Expose
    private Long id;

    @Column(nullable = false)
    @Expose
    private String name;

    @OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord", nullable = false)
    @Expose
    private List<ActorExternalId> externalIds;

    @Nullable
    @Expose
    private String website;

    @Nullable
    @Expose
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "actors_affiliations", joinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "actors_affiliations_actor_id_fk")), inverseJoinColumns = @JoinColumn(name = "affiliation_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "actors_affiliations_affiliation_id_fk")))
    @OrderColumn(name = "ord")
    private List<Actor> affiliations;

    @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY)
    private List<ItemContributor> contributorTo;

    @OneToMany(mappedBy = "actor", fetch = FetchType.LAZY, cascade = CascadeType.ALL , orphanRemoval = true)
    @Nullable
    private List<ActorHistory> history;


    public Actor() {
        this.externalIds = new ArrayList<>();
        this.affiliations = new ArrayList<>();
        this.contributorTo = new ArrayList<>();
    }


    public void addExternalIds(List<ActorExternalId> externalIds) {
        this.externalIds.clear();
        this.externalIds.addAll(externalIds);
    }

    public void addExternalId(ActorExternalId externalIds) {
        this.externalIds.add(externalIds);
    }


    public List<ActorExternalId> getExternalIds() {
        return Collections.unmodifiableList(externalIds);
    }


    public void addToHistory(ActorHistory actorHistory) {
        if (this.history == null)
            this.history = new ArrayList<>();
        this.history.add(actorHistory);
    }
}
