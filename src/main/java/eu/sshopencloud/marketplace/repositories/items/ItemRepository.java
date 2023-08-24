package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends ItemVersionRepository<Item> {

    // problem with the proper clazz_. sometimes abstract class is put
    @Deprecated
    @Query(value =
            "WITH RECURSIVE sub_item AS ( " +
                    "   WITH RECURSIVE merge_item AS (" +
                    "       SELECT v.id, v.merged_with_id, i.id AS itemId" +
                    "       FROM versioned_items v" +
                    "       INNER JOIN items i ON i.persistent_id = v.id" +
                    "       WHERE i.persistent_id = :persistentId and i.id = :versionId " +
                    "       UNION" +
                    "       SELECT v.id, v.merged_with_id, i.id" +
                    "       FROM versioned_items v, merge_item m, items i" +
                    "       WHERE m.id = v.merged_with_id and i.persistent_id = v.id)" +
                    "   SELECT i.persistent_id , i.id,  i.prev_version_id" +
                    "   FROM merge_item m" +
                    "   INNER JOIN items i " +
                    "   ON i.persistent_id  = m.id and m.itemId = i.id" +
                    "   UNION" +
                    "   SELECT i.persistent_id, i.id, i.prev_version_id" +
                    "   FROM items i, sub_item si" +
                    "   WHERE i.id = si.prev_version_id)" +

                    "SELECT i1.id, i1.category, i1.description, i1.label, i1.last_info_update, i1.last_info_update AS date_last_updated," +
                    " i1.source_item_id, i1.status, i1.version, i1.prev_version_id, i1.source_id," +
                    " i1.persistent_id, i1.proposed_version, i1.info_contributor_id, i1.last_info_update AS date_created, 2 AS clazz_" +
                    " FROM items i1 INNER JOIN sub_item si" +
                    " ON i1.persistent_id = si.persistent_id AND si.id = i1.id", nativeQuery = true
    )
    List<Item> findMergedItemsHistory(@Param("persistentId") String persistentId, @Param("versionId") Long versionId);

    @Query(value =
            "WITH RECURSIVE sub_item AS ( " +
                    "   WITH RECURSIVE merge_item AS (" +
                    "       SELECT v.id, v.merged_with_id, i.id AS itemId" +
                    "       FROM versioned_items v" +
                    "       INNER JOIN items i ON i.persistent_id = v.id" +
                    "       WHERE i.persistent_id = :persistentId and i.id = :versionId " +
                    "       UNION" +
                    "       SELECT v.id, v.merged_with_id, i.id" +
                    "       FROM versioned_items v, merge_item m, items i" +
                    "       WHERE m.id = v.merged_with_id and i.persistent_id = v.id)" +
                    "   SELECT i.persistent_id , i.id,  i.prev_version_id" +
                    "   FROM merge_item m" +
                    "   INNER JOIN items i " +
                    "   ON i.persistent_id  = m.id and m.itemId = i.id" +
                    "   UNION" +
                    "   SELECT i.persistent_id, i.id, i.prev_version_id" +
                    "   FROM items i, sub_item si" +
                    "   WHERE i.id = si.prev_version_id)" +

                    "SELECT i1.id" +
                    " FROM items i1 INNER JOIN sub_item si" +
                    " ON i1.persistent_id = si.persistent_id AND i1.id = si.id" +
                    " ORDER BY i1.last_info_update DESC", nativeQuery = true
    )
    List<Long> findItemsHistory(@Param("persistentId") String persistentId, @Param("versionId") Long versionId);


    List<Item> findBySourceId(Long sourceId);

    @Query("select v from Item v join v.contributors c where c.actor.id = :actorId ")
    List<Item> findByContributorActorId(@Param("actorId") Long actorId);


    @Query("select i from Item i inner join ItemMedia m ON m.item.id = i.id WHERE m.concept = :concept")
    List<Item> findAllByMediaConcept(@Param("concept") Concept concept);

    @Query("select i from Item i inner join ItemMedia m ON m.item.id = i.id WHERE m.concept.vocabulary = :vocabulary")
    List<Item> findAllByMediaConceptVocabulary(@Param("vocabulary") String vocabulary);

    boolean existsByMediaConceptVocabularyCode(String vocabularyCode);

    @Query("select i from Item i inner join ItemContributor c ON c.item.id = i.id WHERE c.actor.id = :id ORDER BY i.label")
    List<Item> findAllByContributorsActorId(@Param("id") Long id);

    @Query("select i from Item i left join VersionedItem v ON i.versionedItem = v WHERE  (i.status = 'APPROVED' AND v.active = true) OR (i.proposedVersion = true AND v.active = true)")
    List<Item> findAllItemsToReindex();

    @Query(value = "SELECT i.id FROM items i\n" +
            "            INNER JOIN versioned_items v \n" +
            "            ON v.id = i.persistent_id\n" +
            "            WHERE v.curr_ver_id = i.id \n" +
            "            AND v.status = 'DELETED' AND i.category != 'STEP'", nativeQuery = true)
    List<Long> getDeletedItemsIds();

    @Query(value = " SELECT DISTINCT(v.curr_ver_id) FROM items i" +
            "    INNER JOIN versioned_items v " +
            "    ON v.id = i.persistent_id" +
            "    WHERE i.info_contributor_id = :contributorId", nativeQuery = true)
    List<Long> getContributedItemsIds(Long contributorId);

    @Query("select item from Item item where item.id in :idList AND item.status in :itemStatusList AND item.category <> :toExclude ")
    Page<Item> findByIdInAndStatusIsInExcludeCategory(@Param("idList") List<Long> idList,
            @Param("itemStatusList") List<ItemStatus> itemStatusList, ItemCategory toExclude, Pageable pageable);

    @Query("SELECT i FROM Item i INNER JOIN VersionedItem v ON i.versionedItem = v WHERE i.status=:status" +
            " AND v.active=true AND i.category <> :notCategory AND i.lastInfoUpdate <= :until AND i.lastInfoUpdate >= :from" +
            " ORDER BY i.lastInfoUpdate desc")
    Page<Item> findAllActiveByStatusAndCategoryNotAndLastInfoUpdateGreaterThanEqualAndLastInfoUpdateLessThanEqualOrderByLastInfoUpdateDesc(
            ItemStatus status, ItemCategory notCategory, ZonedDateTime from, ZonedDateTime until, Pageable pageable);

    @Query("SELECT i FROM Item i INNER JOIN VersionedItem v ON i.versionedItem  = v WHERE i.status=:status" +
            " AND v.active=true AND i.category <> :notCategory AND i.lastInfoUpdate >= :from ORDER BY i.lastInfoUpdate desc")
    Page<Item> findAllActiveByStatusAndCategoryNotAndLastInfoUpdateGreaterThanEqualOrderByLastInfoUpdateDesc(
            ItemStatus status, ItemCategory notCategory, ZonedDateTime from, Pageable pageable);

    @Query("SELECT i FROM Item i INNER JOIN VersionedItem v ON i.versionedItem = v WHERE i.status=:status" +
            " AND v.active=true AND i.category <> :notCategory AND i.lastInfoUpdate <= :until ORDER BY i.lastInfoUpdate desc")
    Page<Item> findAllActiveByStatusAndCategoryNotAndLastInfoUpdateLessThanEqualOrderByLastInfoUpdateDesc(ItemStatus status,
            ItemCategory notCategory, ZonedDateTime until, Pageable pageable);

    @Query("SELECT i FROM Item i INNER JOIN VersionedItem v ON i.versionedItem = v WHERE i.status=:status" +
            " AND v.active=true AND i.category <> :notCategory ORDER BY i.lastInfoUpdate desc")
    Page<Item> findAllActiveByStatusAndCategoryNotOrderByLastInfoUpdateDesc(ItemStatus status, ItemCategory notCategory,
            Pageable pageable);

    @Query("SELECT MIN(i.lastInfoUpdate) FROM Item i INNER JOIN VersionedItem v ON i.versionedItem = v WHERE i.status=:status" +
            " AND v.active=true AND i.category <> :notCategory")
    Optional<ZonedDateTime> getMinLastUpdateDateOfActiveItemByStatusAndNotCategory(ItemStatus status,
            ItemCategory notCategory);

    @Query("select i from Item i inner join VersionedItem v ON i.versionedItem = v" +
            " WHERE i.status = :status AND v.active = true AND v.persistentId = :persistentId AND i.category <> :notCategory")
    Optional<Item> findActiveByPersistentIdAndStatusAndCategoryNot(String persistentId, ItemStatus status,
            ItemCategory notCategory);
}
