package eu.sshopencloud.marketplace.model.items;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name = "versioned_items")
@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class VersionedItem {

    @Id
    @Column(name = "id")
    private String persistentId;

    @Version
    @Column(name = "optlock")
    private Long entityVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VersionedItemStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curr_ver_id", foreignKey = @ForeignKey(name = "versioned_item_curr_version_fk"))
    private Item currentVersion;

    @Column(name = "active", nullable = false)
    private boolean active;


    public VersionedItem(String persistentId) {
        this.persistentId = persistentId;
        this.active = true;
    }

    public boolean hasAnyVersions() {
        return (currentVersion != null);
    }
}
