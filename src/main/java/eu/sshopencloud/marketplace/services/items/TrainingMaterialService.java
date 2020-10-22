package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.trainings.TrainingMaterialFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@Slf4j
public class TrainingMaterialService
        extends ItemCrudService<TrainingMaterial, TrainingMaterialDto, PaginatedTrainingMaterials, TrainingMaterialCore> {

    private final TrainingMaterialRepository trainingMaterialRepository;
    private final TrainingMaterialFactory trainingMaterialFactory;


    public TrainingMaterialService(TrainingMaterialRepository trainingMaterialRepository,
                                   TrainingMaterialFactory trainingMaterialFactory,
                                   ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                                   ItemRelatedItemService itemRelatedItemService, PropertyTypeService propertyTypeService,
                                   IndexService indexService) {

        super(itemRepository, versionedItemRepository, itemRelatedItemService, propertyTypeService, indexService);

        this.trainingMaterialRepository = trainingMaterialRepository;
        this.trainingMaterialFactory = trainingMaterialFactory;
    }


    public PaginatedTrainingMaterials getTrainingMaterials(PageCoords pageCoords) {
        return getItemsPage(pageCoords);
    }

    public TrainingMaterialDto getLatestTrainingMaterial(String persistentId) {
        return getLatestItem(persistentId);
    }

    public TrainingMaterialDto getTrainingMaterialVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public TrainingMaterialDto createTrainingMaterial(TrainingMaterialCore trainingMaterialCore) {
        TrainingMaterial trainingMaterial = createItem(trainingMaterialCore);
        return prepareItemDto(trainingMaterial);
    }

    public TrainingMaterialDto updateTrainingMaterial(String persistentId, TrainingMaterialCore trainingMaterialCore) {
        TrainingMaterial trainingMaterial = updateItem(persistentId, trainingMaterialCore);
        return prepareItemDto(trainingMaterial);
    }

    public TrainingMaterialDto revertTrainingMaterial(String persistentId, long versionId) {
        TrainingMaterial trainingMaterial = revertItemVersion(persistentId, versionId);
        return prepareItemDto(trainingMaterial);
    }

    public void deleteTrainingMaterial(String persistentId) {
        deleteItem(persistentId);
    }


    @Override
    protected ItemVersionRepository<TrainingMaterial> getItemRepository() {
        return trainingMaterialRepository;
    }

    @Override
    protected TrainingMaterial makeItem(TrainingMaterialCore trainingMaterialCore, TrainingMaterial prevTrainingMaterial) {
        return trainingMaterialFactory.create(trainingMaterialCore, prevTrainingMaterial);
    }

    @Override
    protected TrainingMaterial makeVersionCopy(TrainingMaterial trainingMaterial) {
        return trainingMaterialFactory.makeNewVersion(trainingMaterial);
    }

    @Override
    protected PaginatedTrainingMaterials wrapPage(Page<TrainingMaterial> trainingMaterialsPage,
                                                  List<TrainingMaterialDto> trainingMaterials) {

        return PaginatedTrainingMaterials.builder()
                .trainingMaterials(trainingMaterials)
                .count(trainingMaterialsPage.getContent().size())
                .hits(trainingMaterialsPage.getTotalElements())
                .page(trainingMaterialsPage.getNumber())
                .perpage(trainingMaterialsPage.getSize())
                .pages(trainingMaterialsPage.getTotalPages())
                .build();
    }

    @Override
    protected TrainingMaterialDto convertItemToDto(TrainingMaterial trainingMaterial) {
        return TrainingMaterialMapper.INSTANCE.toDto(trainingMaterial);
    }

    @Override
    protected String getItemTypeName() {
        return TrainingMaterial.class.getName();
    }
}
