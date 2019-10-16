package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final ItemRelatedItemService itemRelatedItemService;

    public List<TrainingMaterial> getAllTrainingMaterials() {
        return trainingMaterialRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
    }

    public TrainingMaterial getTrainingMaterial(Long id) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        trainingMaterial.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        return trainingMaterial;
    }

}
