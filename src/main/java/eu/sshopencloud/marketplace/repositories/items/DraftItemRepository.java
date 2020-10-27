package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.DraftItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DraftItemRepository extends JpaRepository<DraftItem, Long> {

    Optional<DraftItem> getByItemId(long itemId);
}
