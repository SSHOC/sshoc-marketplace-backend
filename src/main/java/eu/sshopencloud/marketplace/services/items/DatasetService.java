package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.datasets.DatasetCore;
import eu.sshopencloud.marketplace.dto.datasets.DatasetDto;
import eu.sshopencloud.marketplace.dto.datasets.PaginatedDatasets;
import eu.sshopencloud.marketplace.mappers.datasets.DatasetMapper;
import eu.sshopencloud.marketplace.model.datasets.Dataset;
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
                          ItemUpgradeRegistry<Dataset> itemUpgradeRegistry,
                          DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                          PropertyTypeService propertyTypeService, IndexService indexService, UserService userService) {

        super(
                itemRepository, versionedItemRepository, itemUpgradeRegistry, draftItemRepository,
                itemRelatedItemService, propertyTypeService, indexService, userService
        );

        this.datasetRepository = datasetRepository;
        this.datasetFactory = datasetFactory;
    }


    public PaginatedDatasets getDatasets(PageCoords pageCoords) {
        return super.getItemsPage(pageCoords);
    }

    public DatasetDto getDatasetVersion(String persistentId, Long versionId) {
        return super.getItemVersion(persistentId, versionId);
    }

    public DatasetDto getLatestDataset(String persistentId, boolean draft) {
        return getLatestItem(persistentId, draft);
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
    protected String getItemTypeName() {
        return Dataset.class.getName();
    }
}
