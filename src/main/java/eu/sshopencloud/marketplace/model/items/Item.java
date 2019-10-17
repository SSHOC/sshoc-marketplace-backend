package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_generator")
    @SequenceGenerator(name = "item_generator", sequenceName = "items_id_seq", allocationSize = 50)
    private Long id;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Basic
    @Column(nullable = false)
    private String label;

    @Basic
    @Column(nullable = true)
    private String version;

    @Basic
    @Column(nullable = false, length = 4096)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
    @JoinTable(name = "items_licenses", joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "license_code", referencedColumnName = "code"))
    @OrderColumn(name = "ord")
    private List<License> licenses;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @OrderColumn(name = "ord")
    private List<ItemContributor> contributors;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<Property> properties;

    @Basic
    @Column(nullable = true)
    private String accessibleAt;

    /* This field will be handled in a separate manner because in this list should be related items considering all relations and inverses of relations and because of cyclical dependencies */
    @Transient
    private List<ItemRelatedItemInline> relatedItems;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.REMOVE, CascadeType.REFRESH })
    @JoinTable(name = "items_information_contributors", joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @OrderColumn(name = "ord")
    private List<User> informationContributors;

    @Basic
    @Column(nullable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm z", locale = "en_GB")
    private ZonedDateTime lastInfoUpdate;

    @OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH }, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<ItemComment> comments;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JsonIgnore
    private Item prevVersion;

    /* All older versions of this item (except this version). Sorted from the newest. */
    @Transient
    private List<ItemInline> olderVersions;

    /* All newer versions of this item (except this version). Sorted from the oldest. */
    @Transient
    private List<ItemInline> newerVersions;

}
