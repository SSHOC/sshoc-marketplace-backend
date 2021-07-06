package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;


@Repository
public interface ItemRepository extends ItemVersionRepository<Item> {

    Item findByPrevVersionId(Long itemId);

    Item findByPrevVersion(Item item);

    List<Item> findBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);

    @Query(value =
            "WITH RECURSIVE sub_tree AS (\n" +
                    "  SELECT i.id, i.category, i.description, i.label, i.last_info_update,\n" +
                    "    i.source_item_id, i.status, i.version, i.prev_version_id, i.source_id,\n" +
                    "    i.persistent_id, i.proposed_version, i.info_contributor_id, 1 AS clazz_\n" +
                    "  FROM items i\n" +
                    "  WHERE i.id = :versionId \n" +
                    "  UNION\n" +
                    "  SELECT cat.id, cat.category, cat.description, cat.label, cat.last_info_update,\n" +
                    "    cat.source_item_id, cat.status, cat.version, cat.prev_version_id, cat.source_id,\n" +
                    "    cat.persistent_id, cat.proposed_version, cat.info_contributor_id, 2 AS clazz_ \n" +
                    "  FROM items cat\n" +
                    "  INNER JOIN sub_tree st\n" +
                    "  ON cat.id = st.prev_version_id \n" +
                    ")\n" +
                    "SELECT s.id, s.category, s.description, s.label, s.last_info_update, s.last_info_update AS date_last_updated ,\n" +
                    "    s.source_item_id, s.status, s.version, s.prev_version_id, s.source_id,\n" +
                    "    s.persistent_id, s.proposed_version, s.info_contributor_id, s.clazz_, s.last_info_update AS date_created " +
                    "FROM sub_tree s" , nativeQuery = true
    )
    List<Item> findInformationContributorsForVersion(@Param("persistentId") String persistentId, @Param("versionId") Long versionId);

}
