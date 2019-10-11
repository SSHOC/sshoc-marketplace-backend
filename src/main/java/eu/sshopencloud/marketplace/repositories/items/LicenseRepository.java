package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.items.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseRepository extends JpaRepository<License, String> {

}
