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

import static javax.persistence.FetchType.LAZY;


@Entity
@Table(name = "items")
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
    // When using OrderColumn annotation and there is some order index missing in 0..size-1 range,
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

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord", nullable = false)
    private List<ItemExternalId> externalIds;

    @ManyToOne
    @JoinColumn(name = "info_contributor_id", nullable = false)
    private User informationContributor;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord", nullable = false)
    private List<ItemMedia> media;

    @CreationTimestamp
    @Column(nullable = false)
    private ZonedDateTime lastInfoUpdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(nullable = false)
    private boolean proposedVersion;

    @ManyToOne(fetch = LAZY, optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "persistent_id", nullable = false)
    private VersionedItem versionedItem;

    @OneToOne(fetch = LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name="item_prev_version_item_id_fk"))
    private Item prevVersion;


    public Item() {
        this.id = null;
        this.accessibleAt = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.licenses = new ArrayList<>();
        this.contributors = new ArrayList<>();
        this.externalIds = new ArrayList<>();
        this.media = new ArrayList<>();
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

        this.externalIds = baseItem.getExternalIds().stream()
                .map(externalId ->
                        new ItemExternalId(externalId.getIdentifierService(), externalId.getIdentifier(), this)
                )
                .collect(Collectors.toList());

        this.media = baseItem.getMedia().stream()
                .map(media -> new ItemMedia(this, media.getMediaId(), media.getCaption(), media.getItemMediaThumbnail()))
                .collect(Collectors.toList());
    }

    public String getPersistentId() {
        return versionedItem.getPersistentId();
    }

    public boolean isNewestVersion() {
        return (status == ItemStatus.APPROVED && versionedItem.isActive());
    }

    public boolean isProposedVersion() {
        return (proposedVersion && versionedItem.isActive());
    }

    public boolean isDraft() {
        return (status == ItemStatus.DRAFT);
    }

    public boolean isThumbnailOnly(){
        return media.stream().anyMatch(m -> m.getItemMediaThumbnail().equals(ItemMediaType.THUMBNAIL_ONLY));
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

    public void addExternalIds(List<ItemExternalId> externalIds) {
        this.externalIds.clear();
        this.externalIds.addAll(externalIds);
    }

    public List<ItemExternalId> getExternalIds() {
        return Collections.unmodifiableList(externalIds);
    }

    public void addMedia(List<ItemMedia> media) {
        this.media.clear();
        this.media.addAll(media);
    }

    public void addMedia(ItemMedia media) {
        this.media.add(media);
    }


    public List<ItemMedia> getMedia() {
        return Collections.unmodifiableList(media.stream().filter(m -> m.getItemMediaThumbnail()!= ItemMediaType.THUMBNAIL_ONLY).collect(Collectors.toList()));
    }


    public ItemMedia getThumbnail() {
       return media.stream().filter(m -> m.getItemMediaThumbnail() != ItemMediaType.MEDIA).findFirst().orElse(null);
    }

    public boolean isOwner(User user) {
        if (user == null)
            return false;

        return user.equals(informationContributor);
    }
}
