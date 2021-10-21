package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.sources.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends ItemVersionRepository<Item> {

    @Query(
            "select v from Item v " +
                    "join v.versionedItem i " +
                    "where v.status = 'APPROVED' " +
                    "and i.active = true " +
                    "and v.source = :source "
    )
    Page<Item> findAllLatestApprovedItemsForSource(@Param("source") Source source, Pageable page);

    @Query(
            "select v from Item v " +
                    "join v.versionedItem i " +
                    "where v.status = 'APPROVED' " +
                    "and i.active = true " +
                    "and v.source = :source and v.sourceItemId = :sourceItemId"
    )
    Page<Item> findAllLatestApprovedItemsForSource(@Param("source") Source source, @Param("sourceItemId") String sourceItemId, Pageable page);

    @Query(
            "select v from Item v " +
                    "join v.versionedItem i " +
                    "where i.active = true " +
                    "and (v.status = 'APPROVED' or v.proposedVersion = true)" +
                    "and v.source = :source "
    )
    Page<Item> findAllLatestItemsForSource(@Param("source") Source source, Pageable page);

    @Query(
            "select v from Item v " +
                    "join v.versionedItem i " +
                    "where i.active = true " +
                    "and (v.status = 'APPROVED' or v.proposedVersion = true)" +
                    "and v.source = :source and v.sourceItemId = :sourceItemId"
    )
    Page<Item> findAllLatestItemsForSource(@Param("source") Source source, @Param("sourceItemId") String sourceItemId, Pageable page);

    @Query(
            "select v from Item v " +
                    "join v.versionedItem i " +
                    "where i.active = true " +
                    "and (" +
                    "v.status = 'APPROVED' " +
                    "or (v.proposedVersion = true and v.informationContributor = :owner)" +
                    ")" +
                    "and v.source = :source "
    )
    Page<Item> findUserLatestItemsForSource(@Param("source") Source source, @Param("owner") User user, Pageable page);

    @Query(
            "select v from Item v " +
                    "join v.versionedItem i " +
                    "where i.active = true " +
                    "and (" +
                    "v.status = 'APPROVED' " +
                    "or (v.proposedVersion = true and v.informationContributor = :owner)" +
                    ")" +
                    "and v.source = :source and v.sourceItemId = :sourceItemId"
    )
    Page<Item> findUserLatestItemsForSource(@Param("source") Source source, @Param("sourceItemId") String sourceItemId, @Param("owner") User user, Pageable page);


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
    List<Item> findItemHistoryImproved(@Param("versionId") Long versionId);


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

}
