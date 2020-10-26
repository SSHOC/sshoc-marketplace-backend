package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemRepository extends ItemVersionRepository<Item> {

    Item findByPrevVersionId(Long itemId);

    Item findByPrevVersion(Item item);

    List<Item> findBySourceIdAndSourceItemId(Long sourceId, String sourceItemId);
}
