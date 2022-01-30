package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
