package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


@NoRepositoryBean
public interface ItemVersionRepository<T extends Item> extends JpaRepository<T, Long> {

    @Query("select v from #{#entityName} v join VersionedItem i on i.currentVersion = v")
    Page<T> findAllCurrentItems(Pageable pageable);

    @Query("select v from #{#entityName} v join VersionedItem i on i.currentVersion = v where i.persistentId = :persistentId")
    Optional<T> findCurrentVersion(@Param("persistentId") String persistentId);

    @Query(
            "select v from #{#entityName} v " +
                    "join DraftItem d on d.item = v " +
                    "where v.versionedItem.persistentId = :persistentId " +
                    "and d.owner = :draftOwner"
    )
    Optional<T> findDraftVersion(@Param("persistentId") String persistentId, @Param("draftOwner") User draftOwner);

    Optional<T> findByVersionedItemPersistentIdAndId(String persistentId, long id);
    Optional<T> findByVersionedItemPersistentIdAndStatus(String persistentId, ItemStatus status);
}
