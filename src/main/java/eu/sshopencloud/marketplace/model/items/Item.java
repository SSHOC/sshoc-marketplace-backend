package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Entity
@Table(name = "items", uniqueConstraints = {
        @UniqueConstraint(name = "item_prev_version_item_id_uq", columnNames = {"prev_version_id"} )
    })
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@ToString(exclude = {"prevVersion"})
@EqualsAndHashCode(exclude = {"prevVersion"})
public abstract class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_generator")
    @SequenceGenerator(name = "item_generator", sequenceName = "items_id_seq", allocationSize = 50)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Column(nullable = false)
    private String label;

    @Column
    private String version;

    @Column(nullable = false, length = 4096)
    private String description;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "items_licenses",
            joinColumns = @JoinColumn(
                    name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="item_license_item_id_fk")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "license_code", referencedColumnName = "code", foreignKey = @ForeignKey(name="item_license_license_code_fk")
            )
    )
    @OrderColumn(name = "ord")
    private List<License> licenses;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<ItemContributor> contributors;

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinTable(
            name = "items_properties",
            joinColumns = @JoinColumn(
                    name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "items_properties_item_fk")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "property_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "item_properties_property_fk")
            )
    )
    @OrderColumn(name = "ord", nullable = false)
    private List<Property> properties;

    // Hibernate does not handle sparse order properly
    // When using OrderColumn annotation and there is some order index missing from 0..size-1,
    // then a null is inserted
    @Transient
    private boolean sparseProperties = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_links")
    @Column(name = "url", length = 2048)
    @OrderColumn(name = "ord")
    private List<String> accessibleAt;

    @ManyToOne(cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_source_id_fk"))
    private Source source;

    @Column
    private String sourceItemId;

    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(
            name = "items_information_contributors",
            joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    @OrderColumn(name = "ord")
    private List<User> informationContributors;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime lastInfoUpdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'INGESTED'")
    private ItemStatus status;

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinTable(
            name = "items_items_comments",
            joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "item_comment_id", referencedColumnName = "id")
    )
    @OrderBy("dateCreated")
    private List<ItemComment> comments;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "versioned_item_id", nullable = false)
    private VersionedItem versionedItem;

    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_prev_version_item_id_fk"))
    // TODO should be "zaorane" when dealing with items' history task
    private Item prevVersion;


    public Item() {
        this.id = null;
        this.informationContributors = new ArrayList<>();
        this.accessibleAt = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.licenses = new ArrayList<>();
        this.contributors = new ArrayList<>();
    }

    public Item(Item prevVersion) {
        this.id = null;

        this.category = prevVersion.getCategory();
        this.label = prevVersion.getLabel();
        this.version = prevVersion.getVersion();
        this.description = prevVersion.getDescription();
        this.licenses = new ArrayList<>(prevVersion.getLicenses());

        this.contributors = prevVersion.getContributors().stream()
                .map(baseContributor -> new ItemContributor(this, baseContributor))
                .collect(Collectors.toList());

        this.properties = new ArrayList<>(prevVersion.getProperties());
        this.accessibleAt = new ArrayList<>(prevVersion.getAccessibleAt());
        this.source = prevVersion.getSource();
        this.sourceItemId = prevVersion.getSourceItemId();
        this.informationContributors = new ArrayList<>();
        this.comments = new ArrayList<>();

        this.prevVersion = prevVersion;
    }

    public void addInformationContributor(User contributor) {
        if (informationContributors.contains(contributor))
            return;

        informationContributors.add(contributor);
    }

    public List<String> getAccessibleAt() {
        return Collections.unmodifiableList(accessibleAt);
    }

    public void addAccessibleAtLink(String linkUrl) {
        accessibleAt.add(linkUrl);
    }

    public void clearAccessibleAtLinks() {
        accessibleAt.clear();
    }

    public List<Property> getProperties() {
        if (sparseProperties) {
            this.properties.removeIf(Objects::isNull);
            sparseProperties = false;
        }

        return Collections.unmodifiableList(properties);
    }

    public void setProperties(List<Property> properties) {
        this.properties.clear();
        this.properties.addAll(properties);
    }
}
