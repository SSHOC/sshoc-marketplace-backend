package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaginatedTrainingMaterials extends PaginatedResult {

    public PaginatedTrainingMaterials(Page<TrainingMaterial> trainingMaterials, int page, int perpage) {
        this.setTrainingMaterials(trainingMaterials.getContent());
        this.setHits(trainingMaterials.getTotalElements());
        this.setCount(this.getTrainingMaterials().size());
        this.setPage(page);
        this.setPerpage(perpage);
        this.setPages(trainingMaterials.getTotalPages());
    }

    private List<TrainingMaterial> trainingMaterials;

}
