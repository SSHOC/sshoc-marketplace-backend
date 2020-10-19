package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;


@NoRepositoryBean
public interface ItemVersionRepository<T extends Item> extends JpaRepository<T, Long> {

    Page<T> findAllByStatus(ItemStatus itemStatus, Pageable pageable);

    Optional<T> findByVersionedItemPersistentIdAndId(String persistentId, long id);
    Optional<T> findByVersionedItemPersistentIdAndStatus(String persistentId, ItemStatus status);
}
