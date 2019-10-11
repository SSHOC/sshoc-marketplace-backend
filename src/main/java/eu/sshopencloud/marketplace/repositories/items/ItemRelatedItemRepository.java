package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRelatedItemRepository extends JpaRepository<ItemRelatedItem, ItemRelatedItemId> {

    List<ItemRelatedItem> findItemRelatedItemBySubjectId(Long subjectId);

    List<ItemRelatedItem> findItemRelatedItemByObjectId(Long objectId);

}
