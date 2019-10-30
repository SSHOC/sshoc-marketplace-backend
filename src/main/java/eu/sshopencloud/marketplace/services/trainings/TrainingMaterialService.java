package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private  final SearchService searchService;

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

    public void createTrainingMaterials(List<? extends TrainingMaterial> newTrainingMaterials) {
        for (TrainingMaterial newTrainingMaterial: newTrainingMaterials) {
            createTrainingMaterial(newTrainingMaterial);
        }
    }

    public TrainingMaterial createTrainingMaterial(TrainingMaterial newTrainingMaterial) {
        // TODO set previous version by older and newer versions
        newTrainingMaterial.setId(null);
        return saveTrainingMaterial(newTrainingMaterial);
    }

    public TrainingMaterial updateTrainingMaterial(Long id, TrainingMaterial newTrainingMaterial) {
        if (!trainingMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        newTrainingMaterial.setId(id);
        return saveTrainingMaterial(newTrainingMaterial);
    }

    private TrainingMaterial saveTrainingMaterial(TrainingMaterial newTrainingMaterial) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.save(newTrainingMaterial);
        if (itemService.isNewestVersion(trainingMaterial)) {
            if (trainingMaterial.getPrevVersion() != null) {
                searchService.removeItem(trainingMaterial.getPrevVersion());
            }
            searchService.indexItem(trainingMaterial);
        }
        return trainingMaterial;
    }

    public void deleteTrainingMaterial(Long id) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(trainingMaterial);
        itemService.switchVersionForDelete(trainingMaterial);
        trainingMaterialRepository.delete(trainingMaterial);
    }

}
