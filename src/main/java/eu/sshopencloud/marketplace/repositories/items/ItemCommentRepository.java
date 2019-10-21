package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.ItemComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCommentRepository extends JpaRepository<ItemComment, Long> {

}
