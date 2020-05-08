package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item findByPrevVersionId(Long itemId);

    Item findByPrevVersion(Item item);

    Item findByCommentsId(Long commentId);

    List<Item> findBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);

    // TODO
    @Modifying
    @Query(value = "DELETE FROM items WHERE category = 'ACTIVITY'", nativeQuery = true)
    void deleteAllActivityOrphans();

    @Modifying
    @Query(value = "DELETE FROM activities", nativeQuery = true)
    void deleteAllActivityActivitiesOrphans();



    @Modifying
    @Query(value = "DELETE FROM items_information_contributors WHERE item_id IN (SELECT id FROM items WHERE category = 'ACTIVITY')", nativeQuery = true)
    void deleteAllActivityInfoContributorsOrphans();

    @Modifying
    @Query(value = "DELETE FROM items_items_comments WHERE item_id IN (SELECT id FROM items WHERE category = 'ACTIVITY')", nativeQuery = true)
    void deleteAllActivityCommentsOrphans();

    @Modifying
    @Query(value = "DELETE FROM items_licenses WHERE item_id IN (SELECT id FROM items WHERE category = 'ACTIVITY')", nativeQuery = true)
    void deleteAllActivityLicensesOrphans();

    @Modifying
    @Query(value = "DELETE FROM properties WHERE item_id IN (SELECT id FROM items WHERE category = 'ACTIVITY')", nativeQuery = true)
    void deleteAllActivityPropertiesOrphans();

    @Modifying
    @Query(value = "DELETE FROM items_contributors WHERE item_id IN (SELECT id FROM items WHERE category = 'ACTIVITY')", nativeQuery = true)
    void deleteAllActivityContributorsOrphans();


}
