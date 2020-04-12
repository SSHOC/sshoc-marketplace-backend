package eu.sshopencloud.marketplace.conf.startup.trainings;

import eu.sshopencloud.marketplace.conf.startup.items.ItemLoader;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialLoader {

    private final ItemLoader itemLoader;

    private final TrainingMaterialRepository trainingMaterialRepository;

    private  final IndexService indexService;

    public void createTrainingMaterials(String profile, List<TrainingMaterial> newTrainingMaterials) {
        for (TrainingMaterial newTrainingMaterial: newTrainingMaterials) {
            itemLoader.completeProperties(newTrainingMaterial);
            itemLoader.completeContributors(newTrainingMaterial);
            TrainingMaterial trainingMaterial = trainingMaterialRepository.save(newTrainingMaterial);
            if (!profile.equals("prod")) {
                indexService.indexItem(trainingMaterial);
            }
        }
    }

}
