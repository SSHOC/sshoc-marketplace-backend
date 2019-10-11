package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

}
