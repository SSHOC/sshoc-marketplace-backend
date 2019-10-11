package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, String> {

}
