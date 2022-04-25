package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.DraftItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DraftItemRepository extends JpaRepository<DraftItem, Long> {

    Optional<DraftItem> findByItemId(long itemId);

    Page<DraftItem> findByOwner(User owner, Pageable pageable);
    List<DraftItem> findByOwner(User owner);

    int deleteByItemId(long itemId);

}
