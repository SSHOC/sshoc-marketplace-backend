package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.trainings.PaginatedTrainingMaterials;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialCore;
import eu.sshopencloud.marketplace.dto.trainings.TrainingMaterialDto;
import eu.sshopencloud.marketplace.mappers.trainings.TrainingMaterialMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.trainings.TrainingMaterial;
import eu.sshopencloud.marketplace.repositories.items.*;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
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
                                   ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<TrainingMaterial> itemUpgradeRegistry,
                                   DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                                   PropertyTypeService propertyTypeService, IndexService indexService, UserService userService,
                                   MediaStorageService mediaStorageService, SourceService sourceService) {

        super(
                itemRepository, versionedItemRepository, itemVisibilityService, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService, mediaStorageService, sourceService
        );

        this.trainingMaterialRepository = trainingMaterialRepository;
        this.trainingMaterialFactory = trainingMaterialFactory;
    }


    public PaginatedTrainingMaterials getTrainingMaterials(PageCoords pageCoords, boolean approved) {
        return getItemsPage(pageCoords, approved);
    }

    public TrainingMaterialDto getLatestTrainingMaterial(String persistentId, boolean draft, boolean approved) {
        return getLatestItem(persistentId, draft, approved);
    }

    public TrainingMaterialDto getTrainingMaterialVersion(String persistentId, long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public TrainingMaterialDto createTrainingMaterial(TrainingMaterialCore trainingMaterialCore, boolean draft) {
        TrainingMaterial trainingMaterial = createItem(trainingMaterialCore, draft);
        return prepareItemDto(trainingMaterial);
    }

    public TrainingMaterialDto updateTrainingMaterial(String persistentId,
                                                      TrainingMaterialCore trainingMaterialCore,
                                                      boolean draft) {

        TrainingMaterial trainingMaterial = updateItem(persistentId, trainingMaterialCore, draft);
        return prepareItemDto(trainingMaterial);
    }

    public TrainingMaterialDto revertTrainingMaterial(String persistentId, long versionId) {
        TrainingMaterial trainingMaterial = revertItemVersion(persistentId, versionId);
        return prepareItemDto(trainingMaterial);
    }

    public TrainingMaterialDto commitDraftTrainingMaterial(String persistentId) {
        TrainingMaterial trainingMaterial = publishDraftItem(persistentId);
        return prepareItemDto(trainingMaterial);
    }

    public void deleteTrainingMaterial(String persistentId, boolean draft) {
        deleteItem(persistentId, draft);
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
    protected TrainingMaterial modifyItem(TrainingMaterialCore trainingMaterialCore, TrainingMaterial trainingMaterial) {
        return trainingMaterialFactory.modify(trainingMaterialCore, trainingMaterial);
    }

    @Override
    protected TrainingMaterial makeItemCopy(TrainingMaterial trainingMaterial) {
        return trainingMaterialFactory.makeNewVersion(trainingMaterial);
    }

    @Override
    protected PaginatedTrainingMaterials wrapPage(Page<TrainingMaterial> trainingMaterialsPage,
                                                  List<TrainingMaterialDto> trainingMaterials) {

        return PaginatedTrainingMaterials.builder()
                .trainingMaterials(trainingMaterials)
                .count(trainingMaterialsPage.getContent().size())
                .hits(trainingMaterialsPage.getTotalElements())
                .page(trainingMaterialsPage.getNumber() + 1)
                .perpage(trainingMaterialsPage.getSize())
                .pages(trainingMaterialsPage.getTotalPages())
                .build();
    }

    @Override
    protected TrainingMaterialDto convertItemToDto(TrainingMaterial trainingMaterial) {
        return TrainingMaterialMapper.INSTANCE.toDto(trainingMaterial);
    }

    @Override
    protected TrainingMaterialDto convertToDto(Item item) {
        return TrainingMaterialMapper.INSTANCE.toDto(item);
    }

    @Override
    protected String getItemTypeName() {
        return TrainingMaterial.class.getName();
    }

    public List<ItemExtBasicDto> getTrainingMaterialVersions(String persistentId, boolean draft, boolean approved) {
        return getItemHistory(persistentId, getLatestTrainingMaterial(persistentId, draft, approved).getId());
    }

    public List<UserDto> getInformationContributors(String id) {
        return super.getInformationContributors(id);
    }

    public List<UserDto> getInformationContributors(String id, Long versionId) {
        return super.getInformationContributors(id, versionId);
    }

    public TrainingMaterialDto getMerge(String persistentId, List<String> mergeList) {
        return prepareMergeItems(persistentId, mergeList);
    }

    public TrainingMaterialDto merge(TrainingMaterialCore mergeTrainingMaterial, List<String> mergeList) {

        TrainingMaterial trainingMaterial = createItem(mergeTrainingMaterial, false);
        trainingMaterial = mergeItem(trainingMaterial.getPersistentId(), mergeList);
        return prepareItemDto(trainingMaterial);
    }

    public List<SourceDto> getSources(String id) {
        return getAllSources(id);
    }

}
