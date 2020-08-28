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
@ToString(exclude = {"prevVersion", "newPrevVersion"})
@EqualsAndHashCode(exclude = {"prevVersion", "newPrevVersion"})
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

    @Basic
    @Column(nullable = true)
    private String sourceItemId;

    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(name = "items_information_contributors", joinColumns = @JoinColumn(name = "item_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="item_information_contributor_item_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name="item_information_contributor_user_id_fk")))
    @OrderColumn(name = "ord")
    private List<User> informationContributors;

    @Basic
    @Column(nullable = false)
    @CreationTimestamp
    private ZonedDateTime lastInfoUpdate;

    @Basic
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

    /** Needed for switching versions during creating/updating of an item */
    @Transient
    private Item newPrevVersion;


    public Item() {
        this.accessibleAt = new ArrayList<>();
        this.properties = new ArrayList<>();
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
