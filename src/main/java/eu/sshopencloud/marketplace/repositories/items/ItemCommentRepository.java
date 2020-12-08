package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemCommentRepository extends JpaRepository<ItemComment, Long> {

    List<ItemComment> findAllByItemPersistentIdOrderByDateCreatedDesc(String persistentId, Pageable pageable);
}
