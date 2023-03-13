package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemExternalId;
import eu.sshopencloud.marketplace.model.items.ItemSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemExternalIdRepository extends JpaRepository<ItemExternalId, Long> {
    List<ItemExternalId> findAllByIdentifierAndIdentifierService(String identifier, ItemSource source);
}
