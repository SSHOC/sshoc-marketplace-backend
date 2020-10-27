package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.VersionedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VersionedItemRepository extends JpaRepository<VersionedItem, String> {
}
