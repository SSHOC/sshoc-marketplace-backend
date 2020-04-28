package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedTrainingMaterials extends PaginatedResult {

    private List<TrainingMaterialDto> trainingMaterials;

}
