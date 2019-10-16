package eu.sshopencloud.marketplace.repositories.licenses;

import eu.sshopencloud.marketplace.model.licenses.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, String> {

}
