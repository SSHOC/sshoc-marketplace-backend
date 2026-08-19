package eu.sshopencloud.marketplace.model.items;

import eu.sshopencloud.marketplace.model.collections.Collection;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "collections_media")
@Data
@NoArgsConstructor
public class CollectionThumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "collection_media_id_gen")
    @SequenceGenerator(name = "collection_media_id_gen", sequenceName = "collection_media_id_seq", allocationSize = 1)
    private Long id;

    @OneToOne(optional = false)
    private Collection collection;

    private UUID thumbnailId;

    private String caption;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumns(
            value = { @JoinColumn(name = "code"), @JoinColumn(name = "vocabulary_code") },
            foreignKey = @ForeignKey(name="item_media_concept_fk")
    )
    private Concept concept;
}
