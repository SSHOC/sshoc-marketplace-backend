package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class ItemsService extends ItemVersionService<Item> {

    private final ItemRepository itemRepository;

    private final ToolService toolService;
    private final TrainingMaterialService trainingMaterialService;
    private final PublicationService publicationService;
    private final DatasetService datasetService;
    private final WorkflowService workflowService;
    private final StepService stepService;


    public ItemsService(ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                        ItemVisibilityService itemVisibilityService,
                        @Lazy ToolService toolService, @Lazy TrainingMaterialService trainingMaterialService,
                        @Lazy PublicationService publicationService, @Lazy DatasetService datasetService,
                        @Lazy WorkflowService workflowService, @Lazy StepService stepService) {

        super(versionedItemRepository, itemVisibilityService);

        this.itemRepository = itemRepository;
        this.toolService = toolService;
        this.trainingMaterialService = trainingMaterialService;
        this.publicationService = publicationService;
        this.datasetService = datasetService;
        this.workflowService = workflowService;
        this.stepService = stepService;
    }


    public List<ItemBasicDto> getItems(Long sourceId, String sourceItemId) {
        List<Item> items = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId);
        return items.stream().map(ItemConverter::convertItem).collect(Collectors.toList());
    }

    public Item liftItemVersion(String persistentId, boolean draft, boolean changeStatus) {
        Item currentItem = loadCurrentItem(persistentId);

        switch (currentItem.getCategory()) {
            case TOOL_OR_SERVICE:
                return toolService.liftItemVersion(persistentId, draft, changeStatus);

            case TRAINING_MATERIAL:
                return trainingMaterialService.liftItemVersion(persistentId, draft, changeStatus);

            case PUBLICATION:
                return publicationService.liftItemVersion(persistentId, draft, changeStatus);

            case DATASET:
                return datasetService.liftItemVersion(persistentId, draft, changeStatus);

            case WORKFLOW:
                return workflowService.liftItemVersion(persistentId, draft, changeStatus);

            case STEP:
                return stepService.liftItemVersion(persistentId, draft, changeStatus);

            default:
                throw new IllegalStateException(String.format("Unexpected item type: %s", currentItem.getCategory()));
        }
    }


    @Override
    protected Item loadLatestItem(String persistentId) {
        return super.loadLatestItem(persistentId);
    }

    @Override
    protected ItemVersionRepository<Item> getItemRepository() {
        return itemRepository;
    }

    @Override
    protected String getItemTypeName() {
        return Item.class.getName();
    }
}
