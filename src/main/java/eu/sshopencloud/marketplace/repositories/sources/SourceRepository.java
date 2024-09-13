package eu.sshopencloud.marketplace.repositories.sources;

import eu.sshopencloud.marketplace.model.sources.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    public static final String PERSISTENT_ID_COLUMN_NAME = "persistent_id";

    Source findByDomain(String domain);


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
            "  SELECT DISTINCT (s.id), s.domain, s.label, s.last_harvested_date, s.url, s.url_template "+
                    " FROM sources s  " +
                    " INNER JOIN items i" +
                    " ON i.source_id = s.id"+
                    " INNER JOIN sub_item si " +
                    " ON i.persistent_id = si.persistent_id AND si.id = i.id", nativeQuery = true
    )
    List<Source> findSourcesOfItem(@Param("persistentId") String persistentId);

    // problem with returning items with DEPRECATED status
    @Deprecated
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
                    "  SELECT DISTINCT s.id, s.domain, s.label, s.last_harvested_date, s.url, s.url_template, i.source_item_id "+
                    " FROM sources s " +
                    " INNER JOIN items i" +
                    " ON i.source_id = s.id"+
                    " INNER JOIN sub_item si " +
                    " ON i.persistent_id = si.persistent_id AND si.id = i.id", nativeQuery = true
    )
    List<Map<String, Object>> findDetailedSourcesOfItemDeprecated(@Param("persistentId") String persistentId);

    @Query(value =
                    "SELECT s.id, s.domain, s.label, s.last_harvested_date, s.url, s.url_template, i.source_item_id "+
                    " FROM sources s " +
                    " INNER JOIN items i" +
                    " ON i.source_id = s.id"+
                    " WHERE i.status = 'APPROVED' and i.persistent_id = :persistentId", nativeQuery = true
    )
    List<Map<String, Object>> findDetailedSourcesOfItem(@Param("persistentId") String persistentId);

    @Query(value =
            "SELECT s.id, s.domain, s.label, s.last_harvested_date, s.url, s.url_template, i.source_item_id, i.persistent_id "+
                    " FROM sources s " +
                    " INNER JOIN items i" +
                    " ON i.source_id = s.id"+
                    " WHERE i.status = 'APPROVED' and i.persistent_id IN :persistentIds", nativeQuery = true
    )
    List<Map<String, Object>> findDetailedSourcesOfItems(@Param("persistentIds") List<String> persistentIds);
}
