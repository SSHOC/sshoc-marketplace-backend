package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.domain.media.exception.MediaNotAvailableException;
import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.items.*;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.items.ItemExtBasicConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
abstract class ItemCrudService<I extends Item, D extends ItemDto, P extends PaginatedResult<D>, C extends ItemRelationsCore>
        extends ItemVersionService<I> {

    private final ItemRepository itemRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final ItemVisibilityService itemVisibilityService;
    private final DraftItemRepository draftItemRepository;
    private final ItemUpgradeRegistry<I> itemUpgradeRegistry;

    private final ItemRelatedItemService itemRelatedItemService;
    private final PropertyTypeService propertyTypeService;
    private final IndexService indexService;
    private final UserService userService;
    private final MediaStorageService mediaStorageService;
    private final SourceService sourceService;

    public ItemCrudService(ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                           ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<I> itemUpgradeRegistry,
                           DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                           PropertyTypeService propertyTypeService, IndexService indexService, UserService userService,
                           MediaStorageService mediaStorageService, SourceService sourceService) {

        super(versionedItemRepository, itemVisibilityService);

        this.itemRepository = itemRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.itemVisibilityService = itemVisibilityService;
        this.draftItemRepository = draftItemRepository;
        this.itemUpgradeRegistry = itemUpgradeRegistry;

        this.itemRelatedItemService = itemRelatedItemService;
        this.propertyTypeService = propertyTypeService;
        this.indexService = indexService;
        this.userService = userService;

        this.mediaStorageService = mediaStorageService;
        this.sourceService = sourceService;
    }


    protected P getItemsPage(PageCoords pageCoords, boolean approved) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();

        Page<I> itemsPage = loadLatestItems(pageCoords, currentUser, approved);
        List<D> dtos = itemsPage.stream()
                .map(this::prepareItemDto)
                .collect(Collectors.toList());

        return wrapPage(itemsPage, dtos);
    }

    protected D getItemVersion(String persistentId, Long versionId) {
        I item = loadItemVersion(persistentId, versionId);

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!itemVisibilityService.hasAccessToVersion(item, currentUser)) {
            throw new AccessDeniedException(
                    String.format(
                            "User is not authorized to access the given item version with id %s (version id: %d)",
                            persistentId, versionId
                    )
            );
        }

        return prepareItemDto(item);
    }

    protected D getLatestItem(String persistentId, boolean draft, boolean approved) {
        if (draft) {
            I itemDraft = loadItemDraftForCurrentUser(persistentId);
            return prepareItemDto(itemDraft);
        }

        I item = approved ? loadLatestItem(persistentId) : loadLatestItemForCurrentUser(persistentId, true);
        return prepareItemDto(item);
    }


    protected D prepareItemDto(I item) {

        D dto = convertItemToDto(item);

        List<RelatedItemDto> relatedItems = itemRelatedItemService.getItemRelatedItems(item);
        dto.setRelatedItems(relatedItems);

        completeItemDto(dto, item);
        return dto;

    }

    protected I createItem(C itemCore, boolean draft) {
        return createOrUpdateItemVersion(itemCore, null, draft, true);
    }

    protected I mergeItem(String persistentId, List<String> mergedCores) {

        I mergedItem = loadCurrentItem(persistentId);
        mergedItem.getVersionedItem().setMergedWith(new ArrayList<>());

        for (String mergedCore : mergedCores) {
            VersionedItem versionedItem = versionedItemRepository.getOne(mergedCore);
            I prevItem = (I) versionedItem.getCurrentVersion();
            prevItem.setStatus(ItemStatus.DEPRECATED);
            mergedItem.getVersionedItem().addMergedWith(versionedItem);
            itemRepository.save(prevItem);
            versionedItemRepository.save(versionedItem);
        }

        versionedItemRepository.save(mergedItem.getVersionedItem());
        itemRepository.save(mergedItem);
        return mergedItem;

    }

    protected boolean checkIfStep(String itemPersistentId) {
        VersionedItem versionedItem = versionedItemRepository.getOne(itemPersistentId);
        I currItem = (I) versionedItem.getCurrentVersion();
        return currItem.getCategory().equals(ItemCategory.STEP);
    }

    protected boolean checkIfWorkflow(String itemPersistentId) {
        VersionedItem versionedItem = versionedItemRepository.getOne(itemPersistentId);
        I currItem = (I) versionedItem.getCurrentVersion();
        return currItem.getCategory().equals(ItemCategory.WORKFLOW);
    }

    protected I updateItem(String persistentId, C itemCore, boolean draft, boolean approved) {
        I item = loadItemForCurrentUser(persistentId);
        return createOrUpdateItemVersion(itemCore, item, draft, approved);
    }

    private I createOrUpdateItemVersion(C itemCore, I prevVersion, boolean draft, boolean approved) {
        I newItem = prepareAndPushItemVersion(itemCore, prevVersion, draft, approved);
        indexService.indexItem(newItem);
        return newItem;
    }


    private I prepareAndPushItemVersion(C itemCore, I prevVersion, boolean draft, boolean approved) {
        // If there exists a draft item (owned by current user) then it should be modified instead of the current item version
        if (prevVersion != null && prevVersion.getStatus().equals(ItemStatus.DRAFT)) {
            I version = modifyItem(itemCore, prevVersion);
            itemRelatedItemService.updateRelatedItems(itemCore.getRelatedItems(), prevVersion, null, true);

            if (!draft)
                commitItemDraft(version);

            return version;
        }


        I version = makeItemVersion(itemCore, prevVersion);

        version = saveVersionInHistory(version, prevVersion, draft, approved);

        itemRelatedItemService.updateRelatedItems(itemCore.getRelatedItems(), version, prevVersion, draft);

        return version;
    }


    private I saveVersionInHistory(I version, I prevVersion, boolean draft, boolean approved) {
        return saveVersionInHistory(version, prevVersion, draft, true, approved);
    }


    // Warning: important method! Do not change unless you know what you are doing!
    private I saveVersionInHistory(I version, I prevVersion, boolean draft, boolean changeStatus, boolean approved) {
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

        version.setPrevVersion(prevVersion);


        // If not a draft
        if (!draft) {

            itemVisibilityService.setupItemVersionVisibility(version, versionedItem, changeStatus, approved);

            if (version.getStatus() == ItemStatus.APPROVED)
                deprecatePrevApprovedVersion(versionedItem);

            versionedItem.setCurrentVersion(version);
        }
        // If it is a draft
        else {
            version.setStatus(ItemStatus.DRAFT);

            // If it's a first (and draft) version of the item make persistent a draft
            if (versionedItem.getStatus() == null)
                versionedItem.setStatus(VersionedItemStatus.DRAFT);
        }

        version.setVersionedItem(versionedItem);
        version = saveItemVersion(version);

        if (draft) {
            User draftOwner = userService.loadLoggedInUser();
            DraftItem draftItem = new DraftItem(version, prevVersion, draftOwner);

            draftItemRepository.save(draftItem);
        }

        linkItemMedia(version);

        return version;
    }

    private void linkItemMedia(I version) {
        for (ItemMedia media : version.getMedia()) {
            try {
                mediaStorageService.linkToMedia(media.getMediaId());
            } catch (MediaNotAvailableException e) {
                throw new IllegalStateException("Media not available unexpectedly");
            }
        }

        if (Objects.nonNull(version.getThumbnail()) && version.getThumbnail().getItemMediaThumbnail().equals(ItemMediaType.THUMBNAIL_ONLY)) {
            ItemMedia mediaThumbnail = version.getThumbnail();
            try {
                mediaStorageService.linkToMedia(mediaThumbnail.getMediaId());
            } catch (MediaNotAvailableException e) {
                throw new IllegalStateException("Media not available unexpectedly");
            }
        }
    }

    private void deprecatePrevApprovedVersion(VersionedItem versionedItem) {
        Item version = versionedItem.getCurrentVersion();

        while (version != null) {
            if (version.getStatus() == ItemStatus.APPROVED)
                break;

            if (version.isProposedVersion())
                version.setProposedVersion(false);

            version = version.getPrevVersion();
        }

        if (version != null)
            version.setStatus(ItemStatus.DEPRECATED);
    }

    private void copyVersionRelations(I version, I prevVersion) {
        if (prevVersion == null)
            return;

        itemRelatedItemService.copyItemRelations(version, prevVersion);
    }

    protected I publishDraftItem(String persistentId) {
        I draftItem = loadItemDraftForCurrentUser(persistentId);
        I item = commitItemDraft(draftItem);

        indexService.indexItem(item);

        return item;
    }

    protected I commitItemDraft(I version) {
        DraftItem draft = draftItemRepository.findByItemId(version.getId())
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


        itemVisibilityService.setupItemVersionVisibility(version, versionedItem, true, true);

        if (version.getStatus() == ItemStatus.APPROVED)
            deprecatePrevApprovedVersion(versionedItem);

        commitDraftRelations(draft);

        versionedItem.setCurrentVersion(version);
        draftItemRepository.delete(draft);

        return version;
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
        I currentVersion = loadCurrentItem(persistentId);
        I targetVersion = makeItemVersionCopy(item);

        targetVersion = saveVersionInHistory(targetVersion, currentVersion, false, true);
        copyVersionRelations(targetVersion, item);

        indexService.indexItem(targetVersion);

        return targetVersion;
    }

    protected I liftItemVersion(String persistentId, boolean draft) {
        return liftItemVersion(persistentId, draft, true);
    }

    protected I liftItemVersion(String persistentId, boolean draft, boolean modifyStatus) {
        if (draft) {
            Optional<I> itemDraft = resolveItemDraftForCurrentUser(persistentId);
            if (itemDraft.isPresent())
                return itemDraft.get();
        }

        Optional<I> upgradedVersion = itemUpgradeRegistry.resolveUpgradedVersion(persistentId);
        if (upgradedVersion.isPresent())
            return upgradedVersion.get();

        I item = loadCurrentItem(persistentId);
        I newItem = makeItemVersionCopy(item);

        newItem = saveVersionInHistory(newItem, item, draft, modifyStatus, true);
        copyVersionRelations(newItem, item);

        itemUpgradeRegistry.registerUpgradedVersion(newItem);
        indexService.indexItem(newItem);

        return newItem;
    }

    protected void deleteItem(String persistentId, boolean draft) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!draft && !currentUser.isModerator())
            throw new AccessDeniedException("Current user is not a moderator and is not allowed to remove items");

        if (draft) {
            I draftItem = loadItemDraftForCurrentUser(persistentId);
            cleanupDraft(draftItem);

            return;
        }

        I item = loadCurrentItem(persistentId);
        VersionedItem versionedItem = item.getVersionedItem();

        if (versionedItem.getStatus() == VersionedItemStatus.INGESTED
                || versionedItem.getStatus() == VersionedItemStatus.SUGGESTED) {

            versionedItem.setStatus(VersionedItemStatus.REFUSED);
            item.setStatus(ItemStatus.DISAPPROVED);
        } else {
            versionedItem.setStatus(VersionedItemStatus.DELETED);
            versionedItem.setActive(false);
        }

        indexService.removeItemVersions(item);
    }

    private void cleanupDraft(I draftItem) {
        VersionedItem versionedItem = draftItem.getVersionedItem();
        if (!draftItem.getStatus().equals(ItemStatus.DRAFT)) {
            throw new IllegalStateException(
                    String.format(
                            "Unexpected attempt of removing a non-draft item with id %s as a draft item",
                            versionedItem.getPersistentId()
                    )
            );
        }

        unlinkItemMedia(draftItem);

        draftItemRepository.deleteByItemId(draftItem.getId());
        getItemRepository().delete(draftItem);

        if (versionedItem.getStatus().equals(VersionedItemStatus.DRAFT))
            versionedItemRepository.delete(draftItem.getVersionedItem());
    }

    private void unlinkItemMedia(I version) {
        version.getMedia().stream()
                .map(ItemMedia::getMediaId)
                .forEach(mediaStorageService::removeMediaLink);
    }

    private void commitDraftRelations(DraftItem draftItem) {
        itemRelatedItemService.commitDraftRelations(draftItem);
    }


    private void completeItemDto(D dto, I item) {
        completeItemDtoProperties(dto);

        for (int i = 0; i < item.getMedia().size(); ++i) {
            ItemMedia media = item.getMedia().get(i);
            MediaDetails mediaDetails = mediaStorageService.getMediaDetails(media.getMediaId());
            dto.getMedia().get(i).setInfo(mediaDetails);
        }

        ItemMedia thumbnail = item.getThumbnail();
        if (thumbnail != null) {
            MediaDetails mediaDetails = mediaStorageService.getMediaDetails(thumbnail.getMediaId());
            dto.getThumbnail().setInfo(mediaDetails);
        }
    }

    private void completeItemDtoProperties(D dto) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        List<PropertyDto> properties = dto.getProperties()
                .stream()
                .filter(property -> shouldRenderProperty(property, currentUser))
                .peek(property -> propertyTypeService.completePropertyType(property.getType()))
                .collect(Collectors.toList());

        dto.setProperties(properties);
    }

    private boolean shouldRenderProperty(PropertyDto property, User user) {
        // hidden properties have to be always rendered
        //return (!property.getType().isHidden() || (user != null && user.isModerator()));
        return true;
    }

    protected List<ItemExtBasicDto> getItemHistory(String persistentId, Long versionId) {
        I item = loadItemVersion(persistentId, versionId);

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!itemVisibilityService.hasAccessToVersion(item, currentUser)) {
            throw new AccessDeniedException(
                    String.format(
                            "User is not authorized to access the given item version with id %s (version id: %d)",
                            persistentId, versionId
                    )
            );
        }
        return getHistoryOfItemWithMergedWith(item, versionId);
    }

    private List<ItemExtBasicDto> getHistoryOfItemWithMergedWith(Item item, Long versionId) {

        List<ItemExtBasicDto> mergedItemHistoryList = new ArrayList<>(ItemExtBasicConverter.convertItems(itemRepository.findMergedItemsHistory(item.getPersistentId(), versionId)));
        mergedItemHistoryList.sort(Comparator.comparing(ItemExtBasicDto::getLastInfoUpdate).reversed());

        return mergedItemHistoryList;
    }


    protected List<UserDto> getInformationContributors(String itemId) {
        return userService.getInformationContributors(itemId);
    }

    protected List<UserDto> getInformationContributors(String itemId, Long versionId) {
        return userService.getInformationContributors(itemId, versionId);
    }

    protected D prepareMergeItems(String persistentId, List<String> mergeList) {

        List<D> itemDtoList = new ArrayList<>();

        I finalItem = loadLatestItem(persistentId);
        D finalDto = convertItemToDto(finalItem);

        finalDto.setRelatedItems(itemRelatedItemService.getItemRelatedItems(finalItem));
        completeItemDto(finalDto, finalItem);

        for (int i = 0; i < mergeList.size(); i++) {

            I vItem = (I) versionedItemRepository.getOne(mergeList.get(i)).getCurrentVersion();

            D tmp = convertToDto(vItem);
            itemDtoList.add(tmp);

            itemDtoList.get(i).setRelatedItems(itemRelatedItemService.getItemRelatedItems(vItem));
            completeItemDto(itemDtoList.get(i), vItem);


            if (!Objects.isNull(finalDto.getDescription()) && !Objects.isNull(itemDtoList.get(i).getDescription()))
                if (!finalDto.getDescription().contains(itemDtoList.get(i).getDescription()))
                    finalDto.setDescription(finalDto.getDescription() + "/" + itemDtoList.get(i).getDescription());


            if (!Objects.isNull(finalDto.getLabel()) && !Objects.isNull(itemDtoList.get(i).getLabel()))
                if (!finalDto.getLabel().contains(itemDtoList.get(i).getLabel()))
                    finalDto.setLabel(finalDto.getLabel() + "/" + itemDtoList.get(i).getLabel());


            if (!Objects.isNull(finalDto.getVersion()) && !Objects.isNull(itemDtoList.get(i).getVersion()))
                if (!finalDto.getVersion().contains(itemDtoList.get(i).getVersion()))
                    finalDto.setVersion(finalDto.getVersion() + "/" + itemDtoList.get(i).getVersion());


            for (ItemContributorDto e : itemDtoList.get(i).getContributors()) {
                if (!finalDto.getContributors().contains(e))
                    finalDto.getContributors().add(e);
            }


            for (PropertyDto e : itemDtoList.get(i).getProperties()) {
                if (!finalDto.getProperties().contains(e))
                    finalDto.getProperties().add(e);
            }


            for (ItemExternalIdDto e : itemDtoList.get(i).getExternalIds()) {
                if (!finalDto.getExternalIds().contains(e))
                    finalDto.getExternalIds().add(e);
            }


            for (String e : itemDtoList.get(i).getAccessibleAt()) {
                if (!finalDto.getAccessibleAt().contains(e))
                    finalDto.getAccessibleAt().add(e);
            }


            for (RelatedItemDto e : itemDtoList.get(i).getRelatedItems()) {
                if (!finalDto.getRelatedItems().contains(e))
                    finalDto.getRelatedItems().add(e);
            }


            for (ItemMediaDto e : itemDtoList.get(i).getMedia()) {
                if (!finalDto.getMedia().contains(e))
                    finalDto.getMedia().add(e);
            }


        }

        return finalDto;
    }

    public List<SourceDto> getAllSources(String id) {
        return sourceService.getAllSources(id);
    }

    private I makeItemVersion(C itemCore, I prevItem) {
        return makeItem(itemCore, prevItem);
    }

    private I makeItemVersionCopy(I item) {
        return makeItemCopy(item);
    }

    protected I saveItemVersion(I item) {
        return getItemRepository().save(item);
    }


    protected abstract I makeItem(C itemCore, I prevItem);

    protected abstract I modifyItem(C itemCore, I item);

    protected abstract I makeItemCopy(I item);

    protected abstract P wrapPage(Page<I> resultsPage, List<D> convertedDtos);

    protected abstract D convertItemToDto(I item);

    protected abstract D convertToDto(Item item);
}
