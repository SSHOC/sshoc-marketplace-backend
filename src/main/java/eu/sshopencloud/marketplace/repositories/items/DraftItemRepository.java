package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.DraftItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DraftItemRepository extends JpaRepository<DraftItem, Long> {

    Optional<DraftItem> findByItemId(long itemId);

    Page<DraftItem> findByOwner(User owner, Pageable pageable);

    boolean deleteByItemId(long itemId);

    @Query(value = "DELETE FROM draft_items WHERE id = :draftId", nativeQuery = true)
    void deleteDraft(@Param("draftId") long draftId);

}
