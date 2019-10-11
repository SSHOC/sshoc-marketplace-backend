package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.EasyOfUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EasyOfUseRepository extends JpaRepository<EasyOfUse, String> {

}
