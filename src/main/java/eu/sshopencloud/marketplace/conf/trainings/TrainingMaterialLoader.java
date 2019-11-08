package eu.sshopencloud.marketplace.conf.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialLoader {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private  final IndexService indexService;

    public void createTrainingMaterials(List<TrainingMaterial> newTrainingMaterials) {
        for (TrainingMaterial newTrainingMaterial: newTrainingMaterials) {
            TrainingMaterial trainingMaterial = trainingMaterialRepository.save(newTrainingMaterial);
            indexService.indexItem(trainingMaterial);
        }
    }

}
