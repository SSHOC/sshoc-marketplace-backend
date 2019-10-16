package eu.sshopencloud.marketplace.repositories.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {

}
