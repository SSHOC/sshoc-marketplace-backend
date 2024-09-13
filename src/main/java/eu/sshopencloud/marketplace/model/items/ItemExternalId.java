package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;


@Entity
@Table(
        name = "item_external_ids",
        uniqueConstraints = @UniqueConstraint(columnNames = { "identifier_service_code", "identifier", "item_id" })
)
@Data
@EqualsAndHashCode(exclude = "item")
@ToString(exclude = "item")
@NoArgsConstructor
public class ItemExternalId {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_external_id_gen")
    @SequenceGenerator(name = "item_external_id_gen", sequenceName = "item_external_ids_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ItemSource identifierService;

    @Column(nullable = false, length = 2048)
    private String identifier;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(nullable = false)
    private Item item;


    public ItemExternalId(ItemSource identifierService, String identifier, Item item) {
        this.id = null;
        this.identifierService = identifierService;
        this.identifier = identifier;
        this.item = item;
    }
}
