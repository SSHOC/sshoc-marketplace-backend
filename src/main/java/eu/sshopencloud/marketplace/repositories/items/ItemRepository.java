package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends ItemVersionRepository<Item> {

    Item findByPrevVersionId(Long itemId);

    Item findByPrevVersion(Item item);

    List<Item> findBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);

    List<Item> findByVersionedItemMergedWith(String persistentId);

    @Query(value =
            "WITH RECURSIVE sub_tree AS (" +
                    "  SELECT i1.id, i1.category, i1.description, i1.label, i1.last_info_update," +
                    "    i1.source_item_id, i1.status, i1.version, i1.prev_version_id, i1.source_id," +
                    "    i1.persistent_id, i1.proposed_version, i1.info_contributor_id, 1 as clazz_" +
                    "  FROM items i1" +
                    "  WHERE i1.id = :versionId" +
                    "  UNION" +
                    "  SELECT i2.id, i2.category, i2.description, i2.label, i2.last_info_update," +
                    "    i2.source_item_id, i2.status, i2.version, i2.prev_version_id, i2.source_id," +
                    "    i2.persistent_id, i2.proposed_version, i2.info_contributor_id, 2 as clazz_" +
                    "  FROM items i2 INNER JOIN sub_tree st" +
                    "  ON i2.id = st.prev_version_id" +
                    ") " +
                    "SELECT s.id, s.category, s.description, s.label, s.last_info_update, s.last_info_update AS date_last_updated," +
                    "    s.source_item_id, s.status, s.version, s.prev_version_id, s.source_id," +
                    "    s.persistent_id, s.proposed_version, s.info_contributor_id, s.last_info_update AS date_created, s.clazz_" +
                    "    FROM sub_tree s", nativeQuery = true
    )
    List<Item> findItemHistory(@Param("versionId" ) Long versionId);

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
                    "       SELECT v.id, v.merged_with_id" +
                    "       FROM versioned_items v" +
                    "       INNER JOIN items i ON i.persistent_id = v.id" +
                    "       WHERE v.merged_with_id = :persistentId OR i.persistent_id = :persistentId " +
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
                    "SELECT i1.id, i1.category, i1.description, i1.label, i1.last_info_update, i1.last_info_update AS date_last_updated," +
                    " i1.source_item_id, i1.status, i1.version, i1.prev_version_id, i1.source_id," +
                    " i1.persistent_id, i1.proposed_version, i1.info_contributor_id, i1.last_info_update AS date_created, 2 AS clazz_" +
                    " FROM items i1 INNER JOIN sub_item si" +
                    " ON i1.persistent_id = si.persistent_id AND si.id = i1.id", nativeQuery = true
    )
    List<Item> findMergedItemsHistory(@Param("persistentId" ) String persistentId);

}
