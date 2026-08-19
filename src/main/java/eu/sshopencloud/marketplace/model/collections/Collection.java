package eu.sshopencloud.marketplace.model.collections;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.CollectionThumbnail;
import eu.sshopencloud.marketplace.model.vocabularies.Property;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the information about one given collection (of Items)
 */
@Entity
@Table(name = "collections")
@Data
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_generator")
    @SequenceGenerator(name = "item_generator", sequenceName = "items_id_seq")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User owner;

    private String title;

    private String description;

    @OneToMany(
            mappedBy = "collection",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<CollectionItem> collectionItems = new ArrayList<>();

    private boolean visible;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @CreationTimestamp
    private ZonedDateTime updatedAt;

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinTable(
            name = "collection_properties",
            joinColumns = @JoinColumn(
                    name = "collection_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "collection_properties_item_fk")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "property_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "item_properties_property_fk")
            )
    )
    @OrderColumn(name = "ord", nullable = false)
    private List<Property> properties;

    @OneToOne(mappedBy = "collection", cascade =  CascadeType.ALL)
    private CollectionThumbnail thumbnail;
}
