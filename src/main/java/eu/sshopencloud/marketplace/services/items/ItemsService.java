package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.DraftItem;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class ItemsService extends ItemVersionService<Item> {

    private final ItemRepository itemRepository;
    private final DraftItemRepository draftItemRepository;

    private final ToolService toolService;
    private final TrainingMaterialService trainingMaterialService;
    private final PublicationService publicationService;
    private final DatasetService datasetService;
    private final WorkflowService workflowService;
    private final StepService stepService;


    public ItemsService(ItemRepository itemRepository, DraftItemRepository draftItemRepository, VersionedItemRepository versionedItemRepository,
                        ItemVisibilityService itemVisibilityService,
                        @Lazy ToolService toolService, @Lazy TrainingMaterialService trainingMaterialService,
                        @Lazy PublicationService publicationService, @Lazy DatasetService datasetService,
                        @Lazy WorkflowService workflowService, @Lazy StepService stepService) {

        super(versionedItemRepository, itemVisibilityService);

        this.itemRepository = itemRepository;
        this.draftItemRepository = draftItemRepository;
        this.toolService = toolService;
        this.trainingMaterialService = trainingMaterialService;
        this.publicationService = publicationService;
        this.datasetService = datasetService;
        this.workflowService = workflowService;
        this.stepService = stepService;
    }

    public PaginatedItemsBasic getMyDraftItems(ItemOrder order, PageCoords pageCoords) {
        if (order == null) order = ItemOrder.MODIFIED_ON;

        Page<DraftItem> draftItemsPage = draftItemRepository.findByOwner(LoggedInUserHolder.getLoggedInUser(),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(getSortOrderByItemOrder(order))));

        List<ItemBasicDto> items = draftItemsPage.stream().map(draftItem -> ItemConverter.convertItem(draftItem.getItem())).collect(Collectors.toList());

        return PaginatedItemsBasic.builder().items(items)
                .count(draftItemsPage.getContent().size()).hits(draftItemsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(draftItemsPage.getTotalPages())
                .build();
    }

    private Sort.Order getSortOrderByItemOrder(ItemOrder itemOrder) {
        switch (itemOrder) {
            case LABEL:
                if (itemOrder.isAsc()) {
                    return Sort.Order.asc("item.label");
                } else {
                    return Sort.Order.desc("item.label");
                }
            case MODIFIED_ON:
                if (itemOrder.isAsc()) {
                    return Sort.Order.asc("item.lastInfoUpdate");
                } else {
                    return Sort.Order.desc("item.lastInfoUpdate");
                }
            default:
                return Sort.Order.desc("item.lastInfoUpdate");
        }
    }

    public List<ItemBasicDto> getItems(Long sourceId, String sourceItemId) {
        List<Item> items = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId);
        return items.stream().map(ItemConverter::convertItem).collect(Collectors.toList());
    }

    //Eliza - Sortować w bazie
    public PaginatedItemsBasic getItemsBySourceAndSourceItem(Long sourceId, String sourceItemId, PageCoords pageCoords, boolean approved) {

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        Page<Item> itemsPage;

        if (currentUser.isModerator() && !approved) {
            itemsPage = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        } else {
            if (approved || !currentUser.isContributor()) {
                itemsPage = itemRepository.findByStatusAndSourceIdAndSourceItemId(ItemStatus.APPROVED, sourceId, sourceItemId, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));

            } else {

                itemsPage = itemRepository.findBySourceIdAndSourceItemId(sourceId, sourceItemId, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));

                List<Item> finalItemsList = new ArrayList<>();

                itemsPage.forEach(i -> {
                    if (super.checkItemVisibility(i, currentUser))
                        finalItemsList.add(i);
                });

                itemsPage = new PageImpl<>(finalItemsList, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))), finalItemsList.size());

            }
        }

        List<ItemBasicDto> items = itemsPage.stream().map(item -> ItemConverter.convertItem(item)).collect(Collectors.toList());

        //ItemBasicDtoComparator comparator = new ItemBasicDtoComparator();
        // Collections.sort(items, comparator);

        return PaginatedItemsBasic.builder()
                .items(items)
                .count(itemsPage.getContent().size()).hits(itemsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(itemsPage.getTotalPages())
                .build();
    }

    public PaginatedItemsBasic getItemsBySource(Long sourceId, PageCoords pageCoords, boolean approved) {

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        Page<Item> itemsPage;

        if (currentUser.isModerator() && !approved) {
            itemsPage = itemRepository.findBySourceId(sourceId, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        } else {

            if (approved || !currentUser.isContributor()) {
                itemsPage = itemRepository.findByStatusAndSourceId(ItemStatus.APPROVED, sourceId, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));

            } else {
                itemsPage = itemRepository.findBySourceId(sourceId, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
                List<Item> finalItemsList = new ArrayList<>();

                itemsPage.forEach(i -> {
                    if (super.checkItemVisibility(i, currentUser))
                        finalItemsList.add(i);
                });

                itemsPage = new PageImpl<Item>(finalItemsList, PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage()), finalItemsList.size());
            }
        }

        List<ItemBasicDto> items = itemsPage.stream().map(item -> ItemConverter.convertItem(item)).collect(Collectors.toList());

        return PaginatedItemsBasic.builder()
                .items(items)
                .count(itemsPage.getContent().size()).hits(itemsPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(itemsPage.getTotalPages())
                .build();
    }


    public Item liftItemVersion(String persistentId, boolean draft, boolean changeStatus) {
        Item currentItem = loadCurrentItem(persistentId);
        return resolveItemsService(currentItem.getCategory()).liftItemVersion(persistentId, draft, changeStatus);
    }

    private ItemCrudService<? extends Item, ?, ?, ?> resolveItemsService(ItemCategory category) {
        switch (category) {
            case TOOL_OR_SERVICE:
                return toolService;

            case TRAINING_MATERIAL:
                return trainingMaterialService;

            case PUBLICATION:
                return publicationService;

            case DATASET:
                return datasetService;

            case WORKFLOW:
                return workflowService;

            case STEP:
                return stepService;

            default:
                throw new IllegalStateException(String.format("Unexpected item type: %s", category));
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
