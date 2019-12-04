package eu.sshopencloud.marketplace.repositories.datasets;

import eu.sshopencloud.marketplace.model.datasets.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {

}
