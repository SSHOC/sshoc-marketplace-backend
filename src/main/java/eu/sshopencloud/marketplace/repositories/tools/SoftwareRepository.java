package eu.sshopencloud.marketplace.repositories.tools;

import eu.sshopencloud.marketplace.model.tools.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareRepository extends JpaRepository<Software, Long> {

}
