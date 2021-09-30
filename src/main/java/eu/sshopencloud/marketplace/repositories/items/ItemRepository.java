package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends ItemVersionRepository<Item> {

    Item findByPrevVersionId(Long itemId);

    Item findByPrevVersion(Item item);

    List<Item> findBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);

    List<Item> findBySourceId(Long sourceId);

    @Query(value =
                    "SELECT i.id, i.category, i.description, i.label, i.last_info_update, " +
                            "i.last_info_update AS date_last_updated," +
                    " i.source_item_id, i.status, i.version, i.prev_version_id, i.source_id," +
                    " i.persistent_id, i.proposed_version, i.info_contributor_id, i.last_info_update AS date_created " +
                    " FROM items i"
                            +" INNER JOIN versioned_items v" +
                    " ON i.persistent_id = v.id" +
                    " where v.status = 'APPROVED' " +
                            " AND i.source_id = :sourceId and i.source_item_id = :sourceItemId ", nativeQuery = true
    )
    List<Item> findApprovedItemsBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);

    @Query(value =
            "SELECT i.id, i.category, i.description, i.label, i.last_info_update, " +
                    "i.last_info_update AS date_last_updated," +
                    " i.source_item_id, i.status, i.version, i.prev_version_id, i.source_id," +
                    " i.persistent_id, i.proposed_version, i.info_contributor_id, i.last_info_update AS date_created " +
                    " FROM items i"
                    +" INNER JOIN versioned_items v" +
                    " ON i.persistent_id = v.id" +
                    " where v.status = 'APPROVED' " +
                    " AND i.source_id = :sourceId ", nativeQuery = true
    )
    List<Item> findApprovedItemsBySourceId(Long sourceId);

    List<Item> findByVersionedItemMergedWith(String persistentId);

    Page<Item> findBySourceIdAndSourceItemId( Long sourceId, String sourceItemId, Pageable page);

    @Deprecated
    @Query(value =
            "WITH RECURSIVE sub_item AS ( " +
                    "   WITH RECURSIVE merge_item AS (" +
                    "       SELECT v.id, v.merged_with_id" +
                    "       FROM versioned_items v" +
                    "       INNER JOIN items i ON i.persistent_id = v.id" +
                    "       WHERE i.id = :versionId " +
                    "       UNION" +
                    "       SELECT v.id, v.merged_with_id " +
                    "       FROM versioned_items v, merge_item m" +
                    "       WHERE m.id = v.merged_with_id)" +
                    "   SELECT i.persistent_id , i.id,  i.prev_version_id" +
                    "   FROM merge_item m" +
                    "   INNER JOIN items i " +
                    "   ON i.persistent_id  = m.id " +
                    "   UNION" +
                    "   SELECT i.persistent_id, i.id, i.prev_version_id" +
                    "   FROM items i, sub_item si" +
                    "   WHERE i.id = si.prev_version_id)" +
                    "SELECT s.id, s.category, s.description, s.label, s.last_info_update, s.last_info_update AS date_last_updated," +
                    "    s.source_item_id, s.status, s.version, s.prev_version_id, s.source_id," +
                    "    s.persistent_id, s.proposed_version, s.info_contributor_id, s.last_info_update AS date_created, s.clazz_" +
                    "    FROM sub_tree s", nativeQuery = true
    )
    List<Item> findItemHistoryImproved(@Param("versionId" ) Long versionId);


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
    List<Item> findMergedItemsHistory(@Param("persistentId" ) String persistentId, @Param("versionId") Long versionId );

}
