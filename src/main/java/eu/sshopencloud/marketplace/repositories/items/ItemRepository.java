package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item findByPrevVersionId(Long itemId);

    Item findByPrevVersion(Item item);

    Item findByCommentsId(Long commentId);

    List<Item> findBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);

}
