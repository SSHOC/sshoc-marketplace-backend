package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    public PaginatedTrainingMaterials getTrainingMaterials(int page, int perpage) {
        Page<TrainingMaterial> trainingMaterials = trainingMaterialRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (TrainingMaterial trainingMaterial: trainingMaterials) {
            trainingMaterial.setRelatedItems(itemRelatedItemService.getItemRelatedItems(trainingMaterial.getId()));
            trainingMaterial.setOlderVersions(itemService.getOlderVersionsOfItem(trainingMaterial));
            trainingMaterial.setNewerVersions(itemService.getNewerVersionsOfItem(trainingMaterial));
            itemService.fillAllowedVocabulariesForPropertyTypes(trainingMaterial);
        }
        return new PaginatedTrainingMaterials(trainingMaterials, page, perpage);
    }

    public TrainingMaterial getTrainingMaterial(Long id) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        trainingMaterial.setRelatedItems(itemRelatedItemService.getItemRelatedItems(id));
        trainingMaterial.setOlderVersions(itemService.getOlderVersionsOfItem(trainingMaterial));
        trainingMaterial.setNewerVersions(itemService.getNewerVersionsOfItem(trainingMaterial));
        itemService.fillAllowedVocabulariesForPropertyTypes(trainingMaterial);
        return trainingMaterial;
    }

    public TrainingMaterial createTrainingMaterial(TrainingMaterial newTrainingMaterial) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.save(newTrainingMaterial);
        // TODO index in SOLR
        return trainingMaterial;
    }

    public TrainingMaterial updateTrainingMaterial(Long id, TrainingMaterial newTrainingMaterial) {
        // TODO check ID
        newTrainingMaterial.setId(id);
        TrainingMaterial trainingMaterial = trainingMaterialRepository.save(newTrainingMaterial);
        // TODO index in SOLR
        return trainingMaterial;
    }

    public void deleteTrainingMaterial(Long id) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(trainingMaterial);
        itemService.switchVersionForDelete(trainingMaterial);
        trainingMaterialRepository.delete(trainingMaterial);
    }

}
