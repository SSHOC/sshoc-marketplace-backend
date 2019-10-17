package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    public List<TrainingMaterial> getAllTrainingMaterials() {
        List<TrainingMaterial> trainingMaterials = trainingMaterialRepository.findAll(new Sort(Sort.Direction.ASC, "label"));
        for (TrainingMaterial trainingMaterial: trainingMaterials) {
            trainingMaterial.setRelatedItems(itemRelatedItemService.getItemRelatedItems(trainingMaterial.getId()));
            trainingMaterial.setOlderVersions(itemService.getOlderVersionsOfItem(trainingMaterial));
            trainingMaterial.setNewerVersions(itemService.getNewerVersionsOfItem(trainingMaterial));
            itemService.fillAllowedVocabulariesForPropertyTypes(trainingMaterial);
        }
        return trainingMaterials;
    }

    public TrainingMaterial getTrainingMaterial(Long id) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        trainingMaterial.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        trainingMaterial.setOlderVersions(itemService.getOlderVersionsOfItem(trainingMaterial));
        trainingMaterial.setNewerVersions(itemService.getNewerVersionsOfItem(trainingMaterial));
        itemService.fillAllowedVocabulariesForPropertyTypes(trainingMaterial);
        return trainingMaterial;
    }

}
