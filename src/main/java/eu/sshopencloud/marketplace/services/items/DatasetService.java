package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemsDifferencesDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.items.DatasetRepository;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import eu.sshopencloud.marketplace.validators.datasets.DatasetFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class DatasetService extends ItemCrudService<Dataset, DatasetDto, PaginatedDatasets, DatasetCore> {

    private final DatasetRepository datasetRepository;
    private final DatasetFactory datasetFactory;


    public DatasetService(DatasetRepository datasetRepository, DatasetFactory datasetFactory,
                          ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                          ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<Dataset> itemUpgradeRegistry,
                          DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                          PropertyTypeService propertyTypeService, IndexItemService indexItemService, UserService userService,
                          MediaStorageService mediaStorageService, SourceService sourceService, ApplicationEventPublisher eventPublisher,
                          VocabularyService vocabularyService) {

        super(itemRepository, versionedItemRepository, itemVisibilityService, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexItemService, userService, mediaStorageService,
                sourceService, eventPublisher, vocabularyService);

        this.datasetRepository = datasetRepository;
        this.datasetFactory = datasetFactory;
    }


    public PaginatedDatasets getDatasets(PageCoords pageCoords, boolean approved) {
        return getItemsPage(pageCoords, approved);
    }


    public DatasetDto getDatasetVersion(String persistentId, Long versionId) {
        return getItemVersion(persistentId, versionId);
    }


    public DatasetDto getLatestDataset(String persistentId, boolean draft, boolean approved, boolean redirect) {
        return getLatestItem(persistentId, draft, approved, redirect);
    }


    public DatasetDto createDataset(DatasetCore datasetCore, boolean draft) {
        Dataset dataset = createItem(datasetCore, draft);
        return prepareItemDto(dataset);
    }


    public DatasetDto updateDataset(String persistentId, DatasetCore datasetCore, boolean draft, boolean approved)
            throws VersionNotChangedException {
        Dataset dataset = updateItem(persistentId, datasetCore, draft, approved);
        return prepareItemDto(dataset);
    }


    public DatasetDto revertDataset(String persistentId, long versionId) {
        Dataset dataset = revertItemVersion(persistentId, versionId);
        return prepareItemDto(dataset);
    }

    public DatasetDto revertDataset(String persistentId) {
        Dataset dataset = revertItemVersion(persistentId);
        return prepareItemDto(dataset);
    }

    public DatasetDto commitDraftDataset(String persistentId) {
        Dataset dataset = publishDraftItem(persistentId);
        return prepareItemDto(dataset);
    }


    public void deleteDataset(String persistentId, boolean draft) {
        deleteItem(persistentId, draft);
    }

    public void deleteDataset(String persistentId, long versionId) {
        deleteItem(persistentId, versionId);
    }


    @Override
    public DatasetRepository getItemRepository() {
        return datasetRepository;
    }


    @Override
    public Dataset makeItem(DatasetCore datasetCore, Dataset prevDataset, boolean conflict) {
        return datasetFactory.create(datasetCore, prevDataset, conflict);
    }


    @Override
    protected Dataset modifyItem(DatasetCore datasetCore, Dataset dataset) {
        return datasetFactory.modify(datasetCore, dataset);
    }


    @Override
    protected Dataset makeItemCopy(Dataset dataset) {
        return datasetFactory.makeNewVersion(dataset);
    }


    @Override
    public PaginatedDatasets wrapPage(Page<Dataset> datasetsPage, List<DatasetDto> datasets) {
        return PaginatedDatasets.builder().datasets(datasets).count(datasetsPage.getContent().size())
                .hits(datasetsPage.getTotalElements()).page(datasetsPage.getNumber() + 1)
                .perpage(datasetsPage.getSize()).pages(datasetsPage.getTotalPages()).build();
    }


    @Override
    public DatasetDto convertItemToDto(Dataset dataset) {
        DatasetDto dto = DatasetMapper.INSTANCE.toDto(dataset);
        if(LoggedInUserHolder.getLoggedInUser() ==null || !LoggedInUserHolder.getLoggedInUser().isModerator()) {
            dto.getInformationContributor().setEmail(null);
            dto.getContributors().forEach(contributor -> contributor.getActor().setEmail(null));
        }
        return dto;
    }


    @Override
    public DatasetDto convertToDto(Item dataset) {
        DatasetDto dto = DatasetMapper.INSTANCE.toDto(dataset);
        if(LoggedInUserHolder.getLoggedInUser() == null || !LoggedInUserHolder.getLoggedInUser().isModerator()) {
            dto.getInformationContributor().setEmail(null);
            dto.getContributors().forEach(contributor -> contributor.getActor().setEmail(null));
        }
        return dto;
    }


    @Override
    protected String getItemTypeName() {
        return Dataset.class.getName();
    }


    public List<ItemExtBasicDto> getDatasetVersions(String persistentId, boolean draft, boolean approved) {
        return getItemHistory(persistentId, getLatestDataset(persistentId, draft, approved, false).getId());
    }


    public List<UserDto> getInformationContributors(String id) {
        return super.getInformationContributors(id);
    }


    public List<UserDto> getInformationContributors(String id, Long versionId) {
        return super.getInformationContributors(id, versionId);
    }


    public DatasetDto getMerge(String persistentId, List<String> mergeList) {

        return prepareMergeItems(persistentId, mergeList);
    }


    public DatasetDto merge(DatasetCore mergeDataset, List<String> mergeList) throws ItemIsAlreadyMergedException {
        checkIfMergeIsPossible(mergeList);
        Dataset dataset = createItem(mergeDataset, false);
        dataset = mergeItem(dataset.getPersistentId(), mergeList);
        return prepareItemDto(dataset);
    }


    public List<SourceDto> getSources(String persistentId) {
        return getAllSources(persistentId);
    }


    public ItemsDifferencesDto getDifferences(String datasetPersistentId, Long datasetVersionId, String otherPersistentId, Long otherVersionId) {

        return super.getDifferences(datasetPersistentId, datasetVersionId, otherPersistentId, otherVersionId);
    }

}
