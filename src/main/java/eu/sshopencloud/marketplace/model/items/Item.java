package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.licenses.License;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "items", uniqueConstraints = {
        @UniqueConstraint(name = "item_prev_version_item_id_uq", columnNames = {"prev_version_id"} )
    })
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@ToString(of = { "id", "category", "label", "version", "description", "source", "sourceItemId", "status" })
@EqualsAndHashCode(of = { "id", "category", "label", "version", "description", "source", "sourceItemId", "status" })
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

    @ManyToMany
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

    @OneToMany(mappedBy = "item", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
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

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name="item_source_id_fk"))
    private Source source;

    @Column
    private String sourceItemId;

    @ManyToOne
    @JoinTable(
            name = "items_information_contributors",
            joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    private User informationContributor;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime lastInfoUpdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "persistent_id", nullable = false)
    private VersionedItem versionedItem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name="item_prev_version_item_id_fk"))
    private Item prevVersion;


    public Item() {
        this.id = null;
        this.accessibleAt = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.licenses = new ArrayList<>();
        this.contributors = new ArrayList<>();
    }

    public Item(Item baseItem) {
        this.id = null;

        this.category = baseItem.getCategory();
        this.label = baseItem.getLabel();
        this.version = baseItem.getVersion();
        this.description = baseItem.getDescription();
        this.licenses = new ArrayList<>(baseItem.getLicenses());

        this.contributors = baseItem.getContributors().stream()
                .map(baseContributor -> new ItemContributor(this, baseContributor))
                .collect(Collectors.toList());

        this.properties = new ArrayList<>(baseItem.getProperties());
        this.accessibleAt = new ArrayList<>(baseItem.getAccessibleAt());
        this.source = baseItem.getSource();
        this.sourceItemId = baseItem.getSourceItemId();
    }

    public String getPersistentId() {
        return versionedItem.getPersistentId();
    }

    public boolean isNewestVersion() {
        return (status == ItemStatus.APPROVED && versionedItem.isActive());
    }

    public boolean isDraft() {
        return (status == ItemStatus.DRAFT);
    }

    public void setContributors(List<ItemContributor> contributors) {
        this.contributors.clear();
        this.contributors.addAll(contributors);
    }

    public List<Property> getProperties() {
        if (sparseProperties) {
            this.properties.removeIf(Objects::isNull);
            sparseProperties = false;
        }

        return Collections.unmodifiableList(properties);
    }
}
