package eu.sshopencloud.marketplace.model.collections;

import eu.sshopencloud.marketplace.model.auth.User;
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
            orphanRemoval = true
    )
    private List<CollectionItem> collectionItems = new ArrayList<>();

    private boolean visible;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @CreationTimestamp
    private ZonedDateTime updatedAt;
}
