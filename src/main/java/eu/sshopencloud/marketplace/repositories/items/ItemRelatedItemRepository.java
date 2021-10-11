package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItemId;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRelatedItemRepository extends JpaRepository<ItemRelatedItem, ItemRelatedItemId> {

    int countAllBySubjectId(long subjectId);
    List<ItemRelatedItem> findAllBySubjectId(long subjectId);
    List<ItemRelatedItem> findBySubjectIdAndObjectStatus(long subjectId, ItemStatus status);

    int countAllByObjectId(long subjectId);
    List<ItemRelatedItem> findAllByObjectId(long objectId);
    List<ItemRelatedItem> findByObjectIdAndSubjectStatus(long objectId, ItemStatus status);

}
