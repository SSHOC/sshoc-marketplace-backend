package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.VersionedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface VersionedItemRepository extends JpaRepository<VersionedItem, String> {

    Optional<VersionedItem> findByMergedWithPersistentId(String persistentId);

}
