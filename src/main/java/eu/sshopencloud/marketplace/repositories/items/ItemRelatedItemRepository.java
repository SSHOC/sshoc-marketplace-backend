package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItemId;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRelatedItemRepository extends JpaRepository<ItemRelatedItem, ItemRelatedItemId> {

    int countAllBySubjectId(long subjectId);

    List<ItemRelatedItem> findAllBySubjectId(long subjectId);

    List<ItemRelatedItem> findBySubjectIdAndObjectStatus(long subjectId, ItemStatus status);

    ItemRelatedItem findBySubjectIdAndObjectId(long subjectId, long objectId);


    int countAllByObjectId(long subjectId);

    List<ItemRelatedItem> findAllByObjectId(long objectId);

    List<ItemRelatedItem> findByObjectIdAndSubjectStatus(long objectId, ItemStatus status);

    boolean existsByRelation(ItemRelation itemRelation);

    @Modifying
    @Query("delete from ItemRelatedItem p where p.relation = :itemRelation")
    void deleteItemRelatedItemsByOfRelation(@Param("itemRelation") ItemRelation itemRelation);

}
