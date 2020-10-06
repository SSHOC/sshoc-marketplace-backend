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

    @Column(nullable = true)
    private String version;

    @Column(nullable = false, length = 4096)
    private String description;

    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(name = "items_licenses", joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="item_license_item_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "license_code", referencedColumnName = "code", foreignKey = @ForeignKey(name="item_license_license_code_fk")))
    @OrderColumn(name = "ord")
    private List<License> licenses;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "ord")
    private List<ItemContributor> contributors;

    @OneToMany(mappedBy =  "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ord")
    private List<Property> properties;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_links")
    @Column(name = "url", length = 2048)
    @OrderColumn(name = "ord")
    private List<String> accessibleAt;

    @ManyToOne(optional = true, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_source_id_fk"))
    private Source source;

    @Column(nullable = true)
    private String sourceItemId;

    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(name = "items_information_contributors", joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="item_information_contributor_item_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="item_information_contributor_user_id_fk")))
    @OrderColumn(name = "ord")
    private List<User> informationContributors;

    @Column(nullable = false)
    @CreationTimestamp
    private ZonedDateTime lastInfoUpdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'INGESTED'")
    private ItemStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "items_items_comments", joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="items_items_comments_item_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "item_comment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="items_items_comments_item_comment_id_fk")))
    @OrderColumn(name = "ord")
    private List<ItemComment> comments;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
    @JoinColumn(foreignKey = @ForeignKey(name="item_prev_version_item_id_fk"))
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
        this.contributors = new ArrayList<>(prevVersion.getContributors());
        this.properties = new ArrayList<>(prevVersion.getProperties());
        this.accessibleAt = new ArrayList<>(prevVersion.getAccessibleAt());
        this.source = prevVersion.getSource();
        this.sourceItemId = prevVersion.getSourceItemId();
        this.informationContributors = new ArrayList<>();
        this.comments = new ArrayList<>();

        this.prevVersion = prevVersion;
    }

    // TODO some relations are one-to-many and should be changed to many-to-many
    private void reassignReferencedEntities() {
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
        return Collections.unmodifiableList(properties);
    }

    public void setProperties(List<Property> properties) {
        this.properties.clear();
        this.properties.addAll(properties);
        renumberProperties();
    }

    private void renumberProperties() {
        int idx = 0;
        for (Property property : properties)
            property.setOrd(idx++);
    }
}
