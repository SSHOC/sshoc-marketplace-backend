package eu.sshopencloud.marketplace.repositories.tools;

import eu.sshopencloud.marketplace.model.tools.EaseOfUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EaseOfUseRepository extends JpaRepository<EaseOfUse, String> {

}
