package eu.sshopencloud.marketplace.services.trainings;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.trainings.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.trainings.TrainingMaterialValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    private final TrainingMaterialValidator trainingMaterialValidator;

    private final ItemService itemService;

    private final ItemRelatedItemService itemRelatedItemService;

    private final IndexService indexService;


    public PaginatedTrainingMaterials getTrainingMaterials(PageCoords pageCoords) {
        Page<TrainingMaterial> trainingMaterialsPage = trainingMaterialRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<TrainingMaterialDto> trainingMaterials = trainingMaterialsPage.stream().map(TrainingMaterialMapper.INSTANCE::toDto)
                .map(trainingMaterial -> {
                    itemService.completeItem(trainingMaterial);
                    return trainingMaterial;
                })
                .collect(Collectors.toList());

        return PaginatedTrainingMaterials.builder().trainingMaterials(trainingMaterials)
                .count(trainingMaterialsPage.getContent().size()).hits(trainingMaterialsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(trainingMaterialsPage.getTotalPages())
                .build();
    }

    public TrainingMaterialDto getTrainingMaterial(Long id) {
        TrainingMaterial trainingMaterial = trainingMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id));
        return itemService.completeItem(TrainingMaterialMapper.INSTANCE.toDto(trainingMaterial));
    }

    public TrainingMaterialDto createTrainingMaterial(TrainingMaterialCore trainingMaterialCore) throws ValidationException {
        TrainingMaterial trainingMaterial = trainingMaterialValidator.validate(trainingMaterialCore, null);
        itemService.updateInfoDates(trainingMaterial);

        itemService.addInformationContributorToItem(trainingMaterial, LoggedInUserHolder.getLoggedInUser());

        Item nextVersion = itemService.clearVersionForCreate(trainingMaterial);
        trainingMaterial = trainingMaterialRepository.save(trainingMaterial);
        itemService.switchVersion(trainingMaterial, nextVersion);
        indexService.indexItem(trainingMaterial);
        return itemService.completeItem(TrainingMaterialMapper.INSTANCE.toDto(trainingMaterial));
    }

    public TrainingMaterialDto updateTrainingMaterial(Long id, TrainingMaterialCore trainingMaterialCore) throws ValidationException {
        if (!trainingMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        TrainingMaterial trainingMaterial = trainingMaterialValidator.validate(trainingMaterialCore, id);
        itemService.updateInfoDates(trainingMaterial);

        itemService.addInformationContributorToItem(trainingMaterial, LoggedInUserHolder.getLoggedInUser());

        Item prevVersion = trainingMaterial.getPrevVersion();
        Item nextVersion = itemService.clearVersionForUpdate(trainingMaterial);
        trainingMaterial = trainingMaterialRepository.save(trainingMaterial);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.indexItem(trainingMaterial);
        return itemService.completeItem(TrainingMaterialMapper.INSTANCE.toDto(trainingMaterial));
    }

    public void deleteTrainingMaterial(Long id) {
        if (!trainingMaterialRepository.existsById(id)) {
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);
        }
        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);
        itemRelatedItemService.deleteRelationsForItem(trainingMaterial);
        Item prevVersion = trainingMaterial.getPrevVersion();
        Item nextVersion = itemService.clearVersionForDelete(trainingMaterial);
        trainingMaterialRepository.delete(trainingMaterial);
        itemService.switchVersion(prevVersion, nextVersion);
        indexService.removeItem(trainingMaterial);
    }

}
