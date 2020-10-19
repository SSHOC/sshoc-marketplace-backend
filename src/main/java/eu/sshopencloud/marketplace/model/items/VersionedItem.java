package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "versioned_items")
@Data
@NoArgsConstructor
public class VersionedItem {

    @Id
    @Column(name = "id")
    private String persistentId;

    public VersionedItem(String persistentId) {
        this.persistentId = persistentId;
    }
}
