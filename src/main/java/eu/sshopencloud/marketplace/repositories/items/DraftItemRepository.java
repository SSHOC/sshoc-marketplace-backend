package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.DraftItem;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DraftItemRepository extends JpaRepository<DraftItem, Long> {

    Optional<DraftItem> findByItemId(long itemId);

    int deleteByItemId(long itemId);

    @Query("SELECT di from DraftItem di, Item item where di.item=item and di.owner=:user and item.category<>:exclude")
    Page<DraftItem> findAllByOwnerExcludeCategory(User user, ItemCategory exclude, Pageable pageable);
}
