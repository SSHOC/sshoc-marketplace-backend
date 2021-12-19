package eu.sshopencloud.marketplace.repositories.items;

import eu.sshopencloud.marketplace.model.datasets.Dataset;
import org.springframework.stereotype.Repository;


@Repository
public interface DatasetRepository extends ItemVersionRepository<Dataset> {

}
