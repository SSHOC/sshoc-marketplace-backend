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
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;

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
        Page<I> itemsPage = loadLatestItems(pageCoords);
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
            I itemDraft = loadItemDraftForCurrentUser(persistentId);
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
        I item = loadItemForCurrentUser(persistentId);
        return createOrUpdateItemVersion(itemCore, item, draft);
    }

    protected I loadItemForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId)
                .orElseGet(() -> loadCurrentItem(persistentId));
    }

    private I createOrUpdateItemVersion(C itemCore, I prevVersion, boolean draft) {
        I newItem = prepareAndPushItemVersion(itemCore, prevVersion, draft);
        indexService.indexItem(newItem);

        return newItem;
    }

    private I prepareAndPushItemVersion(C itemCore, I prevVersion, boolean draft) {
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
                (prevVersion == null) ? createNewVersionedItem() : prevVersion.getVersionedItem();

        if (!versionedItem.isActive()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Item with id %s has been deleted or merged, so adding a new version is prohibited",
                            versionedItem.getPersistentId()
                    )
            );
        }

        version.setVersionedItem(versionedItem);

        // If not a draft
        if (!draft) {
            versionedItem.setCurrentVersion(version);
            version.setPrevVersion(prevVersion);

            assignItemVersionStatus(version);

            if (prevVersion != null)
                prevVersion.setStatus(ItemStatus.DEPRECATED);
        }
        // If it is a draft
        else {
            version.setStatus(ItemStatus.DRAFT);

            // If it's a first (and draft) version of the item make persistent a draft
            if (versionedItem.hasAnyVersions())
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

        VersionedItem versionedItem = version.getVersionedItem();
        if (versionedItem.getStatus() == VersionedItemStatus.DELETED) {
            throw new IllegalArgumentException(
                    String.format("Cannot commit draft for the deleted/merged item with id %s", versionedItem.getPersistentId())
            );
        }

        assignItemVersionStatus(version);

        versionedItem.setCurrentVersion(version);
        draftItemRepository.delete(draft);

        return version;
    }

    private void assignItemVersionStatus(I version) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!currentUser.isContributor())
            throw new AccessDeniedException("Not authorized to create new item version");

        VersionedItem versionedItem = version.getVersionedItem();

        if (!versionedItem.isActive()) {
            throw new IllegalArgumentException(
                    String.format("Deleted/merged item with id %s cannot be modified anymore", versionedItem.getPersistentId())
            );
        }

        // The order of these role checks does matter as, for example, a moderator is a contributor as well
        if (currentUser.isModerator()) {
            version.setStatus(ItemStatus.APPROVED);
            versionedItem.setStatus(VersionedItemStatus.REVIEWED);
        }
        else if (currentUser.isSystemContributor()) {
            version.setStatus(ItemStatus.INGESTED);

            if (versionedItem.getStatus() != VersionedItemStatus.REVIEWED)
                versionedItem.setStatus(VersionedItemStatus.INGESTED);
        }
        else if (currentUser.isContributor()) {
            version.setStatus(ItemStatus.SUGGESTED);

            if (versionedItem.getStatus() != VersionedItemStatus.REVIEWED)
                versionedItem.setStatus(VersionedItemStatus.SUGGESTED);
        }
    }

    private VersionedItem createNewVersionedItem() {
        String id = resolveNewVersionedItemId();
        return new VersionedItem(id);
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
            Optional<I> itemDraft = resolveItemDraftForCurrentUser(persistentId);
            if (itemDraft.isPresent())
                return itemDraft.get();
        }

        I item = loadCurrentItem(persistentId);
        I newItem = makeVersionCopy(item);

        return saveVersionInHistory(newItem, item, draft);
    }

    protected void deleteItem(String persistentId, boolean draft) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!draft && !currentUser.isModerator())
            throw new AccessDeniedException("Current user is not a moderator and is not allowed to remove items");

        if (draft) {
            I draftItem  = loadItemDraftForCurrentUser(persistentId);
            cleanupDraft(draftItem);

            return;
        }

        I item = loadCurrentItem(persistentId);
        VersionedItem versionedItem = item.getVersionedItem();

        if (versionedItem.getStatus() == VersionedItemStatus.INGESTED
                || versionedItem.getStatus() == VersionedItemStatus.SUGGESTED) {

            versionedItem.setStatus(VersionedItemStatus.REFUSED);
            item.setStatus(ItemStatus.DISAPPROVED);
        }
        else {
            versionedItem.setStatus(VersionedItemStatus.DELETED);
            versionedItem.setActive(false);
        }

        indexService.removeItem(item);
    }

    private void cleanupDraft(I draft) {
        VersionedItem versionedItem = draft.getVersionedItem();
        if (!draft.getStatus().equals(ItemStatus.DRAFT)) {
            throw new IllegalStateException(
                    String.format(
                            "Unexpected attempt of removing a non-draft item with id %s as a draft item",
                            versionedItem.getPersistentId()
                    )
            );
        }

        draftItemRepository.deleteByItemId(draft.getId());
        getItemRepository().delete(draft);

        if (versionedItem.getStatus().equals(VersionedItemStatus.DRAFT))
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