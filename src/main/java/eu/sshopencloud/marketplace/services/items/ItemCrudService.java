package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
abstract class ItemCrudService<I extends Item, D extends ItemDto, P extends PaginatedResult<D>, C> extends ItemVersionService<I> {

    private final ItemRepository itemRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final DraftItemRepository draftItemRepository;
    private final ItemRelatedItemService itemRelatedItemService;
    private final PropertyTypeService propertyTypeService;
    private final IndexService indexService;
    private final UserService userService;


    public ItemCrudService(ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                           DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                           PropertyTypeService propertyTypeService, IndexService indexService, UserService userService) {

        super(versionedItemRepository);

        this.itemRepository = itemRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.draftItemRepository = draftItemRepository;
        this.itemRelatedItemService = itemRelatedItemService;
        this.propertyTypeService = propertyTypeService;
        this.indexService = indexService;
        this.userService = userService;
    }


    protected P getItemsPage(PageCoords pageCoords) {
        PageRequest pageRequest = PageRequest.of(
                pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))
        );

        Page<I> itemsPage = getItemRepository().findAllCurrentItems(pageRequest);
        List<D> dtos = itemsPage.stream()
                .map(this::prepareItemDto)
                .collect(Collectors.toList());

        return wrapPage(itemsPage, dtos);
    }

    protected D getItemVersion(String persistentId, Long versionId) {
        I item = loadItemVersion(persistentId, versionId);
        return prepareItemDto(item);
    }

    protected D getLatestItem(String persistentId, boolean draft) {
        if (draft) {
            I itemDraft = loadItemDraftForCurrentUser(persistentId)
                    .orElseThrow(
                            () -> new EntityNotFoundException(
                                    String.format(
                                            "Unable to find draft %s with id %s for the authorized user",
                                            getItemTypeName(), persistentId
                                    )
                            )
                    );

            return prepareItemDto(itemDraft);
        }

        I item = loadLatestItem(persistentId);
        return prepareItemDto(item);
    }

    protected D prepareItemDto(I item) {
        D dto = convertItemToDto(item);
        return completeItem(dto);
    }

    protected I createItem(C itemCore, boolean draft) {
        return createOrUpdateItemVersion(itemCore, null, draft);
    }

    protected I updateItem(String persistentId, C itemCore, boolean draft) {
        I item = loadItemDraftForCurrentUser(persistentId)
                .orElseGet(() -> loadCurrentItem(persistentId));

        return createOrUpdateItemVersion(itemCore, item, draft);
    }

    private I createOrUpdateItemVersion(C itemCore, I prevVersion, boolean draft) {
        I newItem = enrollItemVersion(itemCore, prevVersion, draft);
        indexService.indexItem(newItem);

        return newItem;
    }

    private I enrollItemVersion(C itemCore, I prevVersion, boolean draft) {
        // If there exists a draft item (owned by current user) then it should be modified instead of the current item version
        if (prevVersion != null && prevVersion.getStatus().equals(ItemStatus.DRAFT)) {
            I version = modifyItem(itemCore, prevVersion);

            if (!draft)
                commitItemDraft(version);

            return version;
        }

        I version = makeItem(itemCore, prevVersion);
        version = saveVersionInHistory(version, prevVersion, draft);

        return version;
    }

    private I saveVersionInHistory(I version, I prevVersion, boolean draft) {
        VersionedItem versionedItem =
                (prevVersion == null) ? createNewVersionedItem(draft) : prevVersion.getVersionedItem();

        version.setVersionedItem(versionedItem);

        // If not a draft
        if (!draft) {
            versionedItem.setCurrentVersion(version);
            versionedItem.setStatus(VersionedItemStatus.REVIEWED);
            version.setPrevVersion(prevVersion);
            version.setStatus(ItemStatus.REVIEWED);

            if (prevVersion != null)
                prevVersion.setStatus(ItemStatus.DEPRECATED);
        }
        // If it is a draft
        else {
            version.setStatus(ItemStatus.DRAFT);

            // If it's a first (and draft) version of the item make persistent a draft
            if (prevVersion == null)
                versionedItem.setStatus(VersionedItemStatus.DRAFT);
        }

        version = getItemRepository().save(version);

        if (draft) {
            User draftOwner = userService.loadLoggedInUser();
            DraftItem draftItem = new DraftItem(version, draftOwner);

            draftItemRepository.save(draftItem);
        }

        return version;
    }

    protected I commitItemDraft(I version) {
        DraftItem draft = draftItemRepository.getByItemId(version.getId())
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "%s with id %s and version id %s cannot be committed as it is not a draft",
                                        getItemTypeName(), version.getVersionedItem().getPersistentId(), version.getId()
                                )

                        )
                );

        version.setStatus(ItemStatus.REVIEWED);

        VersionedItem versionedItem = version.getVersionedItem();
        versionedItem.setCurrentVersion(version);

        if (versionedItem.getStatus().equals(VersionedItemStatus.DRAFT))
            versionedItem.setStatus(VersionedItemStatus.REVIEWED);

        draftItemRepository.delete(draft);

        return version;
    }

    private VersionedItem createNewVersionedItem(boolean draft) {
        String id = resolveNewVersionedItemId();
        VersionedItemStatus status = draft ? VersionedItemStatus.DRAFT : VersionedItemStatus.REVIEWED;

        return new VersionedItem(id, status);
    }

    private String resolveNewVersionedItemId() {
        String id = PersistentId.generated();
        int trials = 0;

        while (versionedItemRepository.existsById(id)) {
            trials++;
            if (trials >= 10)
                throw new RuntimeException("Could not assign an id for the new versioned item");

            id = PersistentId.generated();
        }

        return id;
    }

    protected I revertItemVersion(String persistentId, long versionId) {
        I item = loadItemVersion(persistentId, versionId);
        I latestItem = loadCurrentItem(persistentId);
        I targetVersion = makeVersionCopy(item);

        targetVersion.setPrevVersion(latestItem);
        targetVersion.setVersionedItem(latestItem.getVersionedItem());

        return getItemRepository().save(targetVersion);
    }

    protected I liftItemVersion(String persistentId, boolean draft) {
        if (draft) {
            Optional<I> itemDraft = loadItemDraftForCurrentUser(persistentId);
            if (itemDraft.isPresent())
                return itemDraft.get();
        }

        I item = loadCurrentItem(persistentId);
        I newItem = makeVersionCopy(item);

        return saveVersionInHistory(newItem, item, draft);
    }

    protected void deleteItem(String persistentId) {
        I item = loadCurrentItem(persistentId);

        if (ItemStatus.DRAFT.equals(item.getStatus())) {
            cleanupDraft(item);
        }
        else {
            item.setStatus(ItemStatus.DELETED);
        }

        // TODO removing versioned item as well (setting appropriate status)

        indexService.removeItem(item);
    }

    private void cleanupDraft(I draft) {
        if (!ItemStatus.DRAFT.equals(draft.getStatus()))
            return;

        itemRelatedItemService.deleteRelationsForItem(draft);

        getItemRepository().delete(draft);
        versionedItemRepository.delete(draft.getVersionedItem());
    }

    private List<ItemBasicDto> getNewerVersionsOfItem(Long itemId) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemBasicDto> versions = new ArrayList<>();
        Item nextVersion = itemRepository.findByPrevVersionId(itemId);
        while (nextVersion != null) {
            versions.add(ItemConverter.convertItem(nextVersion));
            nextVersion = itemRepository.findByPrevVersion(nextVersion);
        }
        return versions;
    }

    private List<ItemBasicDto> getOlderVersionsOfItem(Long itemId) {
        // TODO change to recursive subordinates query in ItemRepository
        List<ItemBasicDto> versions = new ArrayList<>();
        Item prevVersion = itemRepository.getOne(itemId).getPrevVersion();
        while (prevVersion != null) {
            versions.add(ItemConverter.convertItem(prevVersion));
            prevVersion = prevVersion.getPrevVersion();
        }
        return versions;
    }

    private D completeItem(D item) {
        item.setRelatedItems(itemRelatedItemService.getItemRelatedItems(item.getId()));
        item.setOlderVersions(getOlderVersionsOfItem(item.getId()));
        item.setNewerVersions(getNewerVersionsOfItem(item.getId()));

        for (PropertyDto property : item.getProperties()) {
            propertyTypeService.completePropertyType(property.getType());
        }

        return item;
    }


    protected abstract I makeItem(C itemCore, I prevItem);
    protected abstract I modifyItem(C itemCore, I item);
    protected abstract I makeVersionCopy(I item);

    protected abstract P wrapPage(Page<I> resultsPage, List<D> convertedDtos);
    protected abstract D convertItemToDto(I item);
}
