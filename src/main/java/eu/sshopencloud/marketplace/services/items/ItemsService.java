package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.dto.items.ItemOrder;
import eu.sshopencloud.marketplace.dto.items.PaginatedItemsBasic;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.model.sources.Source;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.validators.items.ItemContributorFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemsService extends ItemVersionService<Item> {

    private final ItemRepository itemRepository;
    private final DraftItemRepository draftItemRepository;

    private final SourceRepository sourceRepository;

    private final ToolService toolService;
    private final TrainingMaterialService trainingMaterialService;
    private final PublicationService publicationService;
    private final DatasetService datasetService;
    private final WorkflowService workflowService;
    private final StepService stepService;

    private final ItemContributorFactory itemContributorFactory;


    public ItemsService(ItemRepository itemRepository, DraftItemRepository draftItemRepository,
            VersionedItemRepository versionedItemRepository, ItemVisibilityService itemVisibilityService,
            SourceRepository sourceRepository, @Lazy ToolService toolService,
            @Lazy TrainingMaterialService trainingMaterialService, @Lazy PublicationService publicationService,
            @Lazy DatasetService datasetService, @Lazy WorkflowService workflowService, @Lazy StepService stepService,
            ItemContributorFactory itemContributorFactory) {

        super(versionedItemRepository, itemVisibilityService);

        this.itemRepository = itemRepository;
        this.draftItemRepository = draftItemRepository;
        this.sourceRepository = sourceRepository;
        this.toolService = toolService;
        this.trainingMaterialService = trainingMaterialService;
        this.publicationService = publicationService;
        this.datasetService = datasetService;
        this.workflowService = workflowService;
        this.stepService = stepService;
        this.itemContributorFactory = itemContributorFactory;
    }


    public PaginatedItemsBasic getMyDraftItems(ItemOrder order, PageCoords pageCoords) {
        if (order == null)
            order = ItemOrder.MODIFIED_ON;

        Page<DraftItem> draftItemsPage = draftItemRepository.findByOwner(LoggedInUserHolder.getLoggedInUser(),
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(),
                        Sort.by(getSortOrderByItemOrder(order))));

        List<ItemBasicDto> items = draftItemsPage.stream()
                .map(draftItem -> ItemConverter.convertItem(draftItem.getItem())).collect(Collectors.toList());

        return PaginatedItemsBasic.builder().items(items).count(draftItemsPage.getContent().size())
                .hits(draftItemsPage.getTotalElements()).page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(draftItemsPage.getTotalPages()).build();
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


    public PaginatedItemsBasic getItemsBySource(Long sourceId, boolean approved, PageCoords pageCoords) {
        return getItemsBySource(sourceId, null, approved, pageCoords);
    }


    public PaginatedItemsBasic getItemsBySource(Long sourceId, String sourceItemId, boolean approved,
            PageCoords pageCoords) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        Source source = sourceRepository.findById(sourceId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Source.class.getName() + " with id " + sourceId));

        Page<Item> itemsPage = loadLatestItemsForSource(pageCoords, source, sourceItemId, currentUser, approved);

        List<ItemBasicDto> items = itemsPage.stream().map(ItemConverter::convertItem).collect(Collectors.toList());

        return PaginatedItemsBasic.builder().items(items).count(itemsPage.getContent().size())
                .hits(itemsPage.getTotalElements()).page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(itemsPage.getTotalPages()).build();
    }


    private Page<Item> loadLatestItemsForSource(PageCoords pageCoords, Source source, String sourceItemId, User user,
            boolean approved) {
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(),
                Sort.by(Sort.Order.asc("label")));

        if (approved || user == null) {
            if (sourceItemId == null) {
                return itemRepository.findAllLatestApprovedItemsForSource(source, pageRequest);
            } else {
                return itemRepository.findAllLatestApprovedItemsForSource(source, sourceItemId, pageRequest);
            }
        }

        if (user.isModerator()) {
            if (sourceItemId == null) {
                return itemRepository.findAllLatestItemsForSource(source, pageRequest);
            } else {
                return itemRepository.findAllLatestItemsForSource(source, sourceItemId, pageRequest);
            }
        }

        if (sourceItemId == null) {
            return itemRepository.findUserLatestItemsForSource(source, user, pageRequest);
        } else {
            return itemRepository.findUserLatestItemsForSource(source, sourceItemId, user, pageRequest);
        }
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


    public void replaceActors(Actor actor, Actor other) {
        List<Item> items = itemRepository.findByContributorActorId(other.getId());
        items.forEach(item -> {
            replaceItemContributor(item, actor, other);
        });

    }


    public boolean checkIfContainsItemContributor(List<ItemContributor> contributors, ItemContributor itemContributor) {
        AtomicBoolean check = new AtomicBoolean(false);

        contributors.forEach(c -> {
            if (c.getActor().getId().equals(itemContributor.getActor().getId()) && c.getItem()
                    .equals(itemContributor.getItem()) && c.getRole().equals(itemContributor.getRole()))
                check.set(true);
        });
        return check.get();

    }


    public void replaceItemContributor(Item item, Actor actor, Actor other) {
        List<ItemContributor> contributorsToReplace = new ArrayList<>();
        List<ItemContributor> contributorsToRemove = new ArrayList<>();

        item.getContributors().forEach(c -> {
            if (c.getActor().equals(other)) {

                contributorsToRemove.add(c);
                ItemContributor itemContributor = new ItemContributor(item, actor, c.getRole(), c.getOrd());

                if (!checkIfContainsItemContributor(item.getContributors(), itemContributor))
                    contributorsToReplace.add(itemContributor);
            }
        });

        if (contributorsToRemove.size() > 0) {
            item.getContributors().removeAll(contributorsToRemove);
            itemRepository.save(item);
        }

        if (contributorsToReplace.size() > 0) {
            Set<Map.Entry<Long, String>> actorRoles = new HashSet<>();
            List<ItemContributor> finalContributorsToAdd = new ArrayList<>(item.getContributors());

            item.getContributors().forEach(c -> {
                if (!Objects.isNull(c.getActor()) && !Objects.isNull(c.getRole()))
                    actorRoles.add(Map.entry(c.getActor().getId(), c.getRole().getCode()));
            });

            contributorsToReplace.forEach(c -> {
                if (!actorRoles.contains(Map.entry(c.getActor().getId(), c.getRole().getCode())))
                    finalContributorsToAdd.add(c);
            });

            setItemContributors(item.getPersistentId(), finalContributorsToAdd);
        }
    }


    public void setItemContributors(String persistentId, List<ItemContributor> finalContributorsToAdd) {
        Item i = loadLatestItem(persistentId);
        i.setContributors(finalContributorsToAdd);
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
