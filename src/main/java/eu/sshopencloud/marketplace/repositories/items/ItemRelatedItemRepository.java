package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemRelatedItem;
import eu.sshopencloud.marketplace.model.items.ItemRelatedItemId;
import eu.sshopencloud.marketplace.model.items.ItemRelation;
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

    int countAllByObjectId(long subjectId);

    @Query("SELECT i.subject.id, count(i.subject) FROM ItemRelatedItem i WHERE i.subject.id in :ids group by i.subject.id")
    List<Long[]> countAllBySubject(List<Long> ids);

    @Query("SELECT i.object.id, count(i.object) FROM ItemRelatedItem i WHERE i.object.id in :ids group by i.object.id")
    List<Long[]> countAllByObject(List<Long> ids);

    List<ItemRelatedItem> findAllByObjectId(long objectId);

    boolean existsByRelation(ItemRelation itemRelation);

    @Modifying
    @Query("delete from ItemRelatedItem p where p.relation = :itemRelation")
    void deleteItemRelatedItemsByOfRelation(@Param("itemRelation") ItemRelation itemRelation);

}
