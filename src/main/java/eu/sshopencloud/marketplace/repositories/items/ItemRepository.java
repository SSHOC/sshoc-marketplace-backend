package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item findItemByPrevVersion(Item item);

    Item findItemByCommentsId(Long commentId);

}
