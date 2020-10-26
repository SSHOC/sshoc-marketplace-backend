package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name = "versioned_items")
@Data
@NoArgsConstructor
public class VersionedItem {

    @Id
    @Column(name = "id")
    private String persistentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionedItemStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curr_ver_id", foreignKey = @ForeignKey(name = "versioned_item_curr_version_fk"))
    private Item currentVersion;


    public VersionedItem(String persistentId, VersionedItemStatus status) {
        this.persistentId = persistentId;
        this.status = status;
    }
}
