package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DatasetRepository extends ItemVersionRepository<Dataset> {

    Page<Dataset> findAllByStatus(ItemStatus status, Pageable pageable);

    Optional<Dataset> findByVersionedItemPersistentIdAndId(String persistentId, long id);
    Optional<Dataset> findByVersionedItemPersistentIdAndStatus(String persistentId, ItemStatus status);
}
