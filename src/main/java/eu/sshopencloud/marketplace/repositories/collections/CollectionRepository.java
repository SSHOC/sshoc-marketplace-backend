package eu.sshopencloud.marketplace.repositories.collections;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.collections.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Page<Collection> findAllByVisibleTrue(PageRequest pageRequest);

    Page<Collection> findAllByOwner(User owner, PageRequest pageRequest);
}
