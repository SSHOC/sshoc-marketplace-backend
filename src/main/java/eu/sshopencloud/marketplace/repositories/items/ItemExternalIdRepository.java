package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemExternalIdRepository extends JpaRepository<ItemExternalId, Long> {
    Optional<ItemExternalId> findByIdentifierAndIdentifierServiceAndItem(String identifier, ItemSource source, Item item);
}
