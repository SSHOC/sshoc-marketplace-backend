package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.dto.items.ItemExtBasicDto;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.items.DatasetRepository;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.validators.datasets.DatasetFactory;
import lombok.extern.slf4j.Slf4j;
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
                          PropertyTypeService propertyTypeService, IndexService indexService, UserService userService,
                          MediaStorageService mediaStorageService) {

        super(
                itemRepository, versionedItemRepository, itemVisibilityService, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService, mediaStorageService
        );

        this.datasetRepository = datasetRepository;
        this.datasetFactory = datasetFactory;
    }


    public PaginatedDatasets getDatasets(PageCoords pageCoords, boolean approved) {
        return getItemsPage(pageCoords, approved);
    }

    public DatasetDto getDatasetVersion(String persistentId, Long versionId) {
        return getItemVersion(persistentId, versionId);
    }

    public DatasetDto getLatestDataset(String persistentId, boolean draft, boolean approved) {
        return getLatestItem(persistentId, draft, approved);
    }

    public DatasetDto createDataset(DatasetCore datasetCore, boolean draft) {
        Dataset dataset = createItem(datasetCore, draft);
        return prepareItemDto(dataset);
    }

    public DatasetDto updateDataset(String persistentId, DatasetCore datasetCore, boolean draft) {
        Dataset dataset = updateItem(persistentId, datasetCore, draft);
        return prepareItemDto(dataset);
    }

    public DatasetDto revertDataset(String persistentId, long versionId) {
        Dataset dataset = revertItemVersion(persistentId, versionId);
        return prepareItemDto(dataset);
    }

    public DatasetDto commitDraftDataset(String persistentId) {
        Dataset dataset = publishDraftItem(persistentId);
        return prepareItemDto(dataset);
    }

    public void deleteDataset(String persistentId, boolean draft) {
        super.deleteItem(persistentId, draft);
    }


    @Override
    public DatasetRepository getItemRepository() {
        return datasetRepository;
    }

    @Override
    public Dataset makeItem(DatasetCore datasetCore, Dataset prevDataset) {
        return datasetFactory.create(datasetCore, prevDataset);
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
        return PaginatedDatasets.builder().datasets(datasets)
                .count(datasetsPage.getContent().size())
                .hits(datasetsPage.getTotalElements())
                .page(datasetsPage.getNumber() + 1)
                .perpage(datasetsPage.getSize())
                .pages(datasetsPage.getTotalPages())
                .build();
    }

    @Override
    public DatasetDto convertItemToDto(Dataset dataset) {
        return DatasetMapper.INSTANCE.toDto(dataset);
    }

    @Override
    public DatasetDto convertToDto(Item dataset) {
        return DatasetMapper.INSTANCE.toDto(dataset);
    }


    @Override
    protected String getItemTypeName() {
        return Dataset.class.getName();
    }

    public List<ItemExtBasicDto> getDatasetVersions(String persistentId, boolean draft, boolean approved) {
        return getItemHistory(persistentId, getLatestDataset(persistentId, draft, approved).getId());
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

    public DatasetDto merge(DatasetCore mergeDataset, List<String> mergeCores) {
        //stwórz nowy obiekt
        //zmień statusy w item na REVIEWED i APPROVED
        //zmień statusy w verisonedItem na MERGED  i DEPRECATED
        Dataset dataset = createItem(mergeDataset, false);

        dataset = mergeItem22(mergeDataset, dataset.getPersistentId(), mergeCores);

        //Dataset dataset = mergeItem(mergeDataset, mergeCores);
        System.out.println("E " + mergeCores);
        System.out.println("E " + dataset.getStatus().toString());
        System.out.println("E " + dataset.getVersionedItem().getStatus());
        //dlaczego sie duplikuje ?????
      //  dataset.getVersionedItem().getMergedWith().forEach( (n) -> System.out.println(n.getPersistentId()));
        //System.out.println("E");} );
        //return null;
       return prepareItemDto2(dataset);
    }

}
