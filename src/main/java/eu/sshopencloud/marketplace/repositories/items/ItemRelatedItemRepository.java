package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItemId;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRelatedItemRepository extends JpaRepository<ItemRelatedItem, ItemRelatedItemId> {

    List<ItemRelatedItem> findAllBySubjectId(long subjectId);
    List<ItemRelatedItem> findBySubjectIdAndObjectStatusOrderByObjectId(long subjectId, ItemStatus status);

    List<ItemRelatedItem> findAllByObjectId(long objectId);
    List<ItemRelatedItem> findByObjectIdAndSubjectStatusOrderBySubjectId(long objectId, ItemStatus status);

}
