package eu.sshopencloud.marketplace.repositories.licenses;

import eu.sshopencloud.marketplace.model.licenses.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseRepository extends JpaRepository<License, String> {

}
