package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


@NoRepositoryBean
public interface ItemVersionRepository<T extends Item> extends JpaRepository<T, Long> {

    @Query(
            "select v from #{#entityName} v " +
                    "join v.versionedItem i " +
                    "where v.status = 'APPROVED' " +
                    "and i.active = true"
    )
    Page<T> findAllLatestApprovedItems(Pageable page);

    @Query(
            "select v from #{#entityName} v " +
                    "join v.versionedItem i " +
                    "where i.active = true " +
                    "and (v.status = 'APPROVED' or v.proposedVersion = true)"
    )
    Page<T> findAllLatestItems(Pageable page);

    @Query(
            "select v from #{#entityName} v " +
                    "join v.versionedItem i " +
                    "where i.active = true " +
                    "and (" +
                    "v.status = 'APPROVED' " +
                    "or (v.proposedVersion = true and v.informationContributor = :owner)" +
                    ")"
    )
    Page<T> findUserLatestItems(@Param("owner") User user, Pageable page);

    @Query(
            "select v from #{#entityName} v " +
                    "join v.versionedItem i " +
                    "where v.status = 'APPROVED' " +
                    "and i.persistentId = :persistentId " +
                    "and i.active = true"
    )
    Optional<T> findLatestItem(@Param("persistentId") String persistentId);


    @Query(
            "select v from #{#entityName} v " +
                    "join VersionedItem i on i.currentVersion = v " +
                    "where i.persistentId = :persistentId " +
                    "and i.active = true"
    )
    Optional<T> findCurrentActiveVersion(@Param("persistentId") String persistentId);

    @Query(
            "select v from #{#entityName} v " +
                    "join VersionedItem i on i.currentVersion = v " +
                    "where i.persistentId = :persistentId"
    )
    Optional<T> findCurrentVersion(@Param("persistentId") String persistentId);

    @Query(
            "select v from #{#entityName} v " +
                    "join DraftItem d on d.item = v " +
                    "where v.versionedItem.persistentId = :persistentId " +
                    "and d.owner = :draftOwner"
    )
    Optional<T> findDraftVersion(@Param("persistentId") String persistentId, @Param("draftOwner") User draftOwner);

    Optional<T> findByVersionedItemPersistentIdAndId(String persistentId, long id);

    List<T> findByVersionedItemPersistentId(String persistentId);

    @Query(
            "select v from #{#entityName} v " +
                    "join v.versionedItem i " +
                    "where i.status = 'MERGED' " +
                    "and i.persistentId = :persistentId " +
                    "and v.status = 'DEPRECATED' "
    )
    List<T> findIfMergedItem(@Param("persistentId") String persistentId);

    @Query( value =
            "SELECT DISTINCT(i2.persistent_id) FROM items i " +
                    "Inner join versioned_items v ON i.persistent_id = v.id " +
                    "INNER JOIN items i2 ON v.merged_with_id = i2.persistent_id  " +
                    "WHERE v.id = :persistentId AND i.status = 'DEPRECATED' AND v.status = 'MERGED' ",  nativeQuery = true
    )
    String findMergedWithPersistentId(@Param("persistentId") String persistentId);
}
