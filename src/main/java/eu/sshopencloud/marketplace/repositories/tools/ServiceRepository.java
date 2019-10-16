package eu.sshopencloud.marketplace.repositories.tools;

import eu.sshopencloud.marketplace.model.tools.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

}
