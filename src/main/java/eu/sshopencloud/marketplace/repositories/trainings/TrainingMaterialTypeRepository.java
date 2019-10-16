package eu.sshopencloud.marketplace.repositories.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingMaterialTypeRepository extends JpaRepository<TrainingMaterialType, String> {

}
