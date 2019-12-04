package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
public class PaginatedTrainingMaterials extends PaginatedResult {

    private List<TrainingMaterial> trainingMaterials;

}
