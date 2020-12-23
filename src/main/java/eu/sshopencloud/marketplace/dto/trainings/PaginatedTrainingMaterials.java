package eu.sshopencloud.marketplace.dto.trainings;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedTrainingMaterials extends PaginatedResult<TrainingMaterialDto> {

    private List<TrainingMaterialDto> trainingMaterials;

    @Override
    @JsonGetter("trainingMaterials")
    public List<TrainingMaterialDto> getResults() {
        return trainingMaterials;
    }
}
