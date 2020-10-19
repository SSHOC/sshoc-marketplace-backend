package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.items.TrainingMaterialRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.validators.ValidationException;
import eu.sshopencloud.marketplace.validators.trainings.TrainingMaterialFactory;
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
    private final TrainingMaterialFactory trainingMaterialFactory;
    private final ItemCrudService itemService;
    private final IndexService indexService;


    public PaginatedTrainingMaterials getTrainingMaterials(PageCoords pageCoords) {
        Page<TrainingMaterial> trainingMaterialsPage = trainingMaterialRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<TrainingMaterialDto> trainingMaterials = trainingMaterialsPage.stream()
                .map(TrainingMaterialMapper.INSTANCE::toDto)
                .map(itemService::completeItem)
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
        return createNewTrainingMaterialVersion(trainingMaterialCore, null);
    }

    public TrainingMaterialDto updateTrainingMaterial(Long id, TrainingMaterialCore trainingMaterialCore)
            throws ValidationException {

        if (!trainingMaterialRepository.existsById(id))
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);

        return createNewTrainingMaterialVersion(trainingMaterialCore, id);
    }

    private TrainingMaterialDto createNewTrainingMaterialVersion(TrainingMaterialCore trainingMaterialCore,
                                                                 Long prevTrainingMaterialId) throws ValidationException {

        TrainingMaterial prevTrainingMaterial =
                (prevTrainingMaterialId != null) ? trainingMaterialRepository.getOne(prevTrainingMaterialId) : null;

        TrainingMaterial trainingMaterial = trainingMaterialFactory.create(trainingMaterialCore, prevTrainingMaterial);

        trainingMaterial = trainingMaterialRepository.save(trainingMaterial);
        indexService.indexItem(trainingMaterial);

        return itemService.completeItem(TrainingMaterialMapper.INSTANCE.toDto(trainingMaterial));
    }

    public void deleteTrainingMaterial(Long id) {
        if (!trainingMaterialRepository.existsById(id))
            throw new EntityNotFoundException("Unable to find " + TrainingMaterial.class.getName() + " with id " + id);

        TrainingMaterial trainingMaterial = trainingMaterialRepository.getOne(id);

        itemService.cleanupItem(trainingMaterial);
        trainingMaterialRepository.delete(trainingMaterial);
        indexService.removeItem(trainingMaterial);
    }
}
