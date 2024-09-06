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
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyTypeDto;
import eu.sshopencloud.marketplace.dto.workflows.WorkflowDto;
import eu.sshopencloud.marketplace.mappers.items.ItemExtBasicConverter;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.*;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.items.DraftItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import eu.sshopencloud.marketplace.services.auth.UserService;
import eu.sshopencloud.marketplace.services.items.event.ItemsMergedEvent;
import eu.sshopencloud.marketplace.services.items.exception.ItemIsAlreadyMergedException;
import eu.sshopencloud.marketplace.services.items.exception.VersionNotChangedException;
import eu.sshopencloud.marketplace.services.search.IndexItemService;
import eu.sshopencloud.marketplace.services.sources.SourceService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.VocabularyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
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
    private final IndexItemService indexItemService;
    private final UserService userService;
    private final MediaStorageService mediaStorageService;
    private final SourceService sourceService;

    private final ApplicationEventPublisher eventPublisher;
    private final VocabularyService vocabularyService;


    public ItemCrudService(ItemRepository itemRepository, VersionedItemRepository versionedItemRepository,
                           ItemVisibilityService itemVisibilityService, ItemUpgradeRegistry<I> itemUpgradeRegistry,
                           DraftItemRepository draftItemRepository, ItemRelatedItemService itemRelatedItemService,
                           PropertyTypeService propertyTypeService, IndexItemService indexItemService, UserService userService,
                           MediaStorageService mediaStorageService, SourceService sourceService,
                           ApplicationEventPublisher eventPublisher, VocabularyService vocabularyService) {

        super(versionedItemRepository, itemVisibilityService);

        this.itemRepository = itemRepository;
        this.versionedItemRepository = versionedItemRepository;
        this.itemVisibilityService = itemVisibilityService;
        this.draftItemRepository = draftItemRepository;
        this.itemUpgradeRegistry = itemUpgradeRegistry;

        this.itemRelatedItemService = itemRelatedItemService;
        this.propertyTypeService = propertyTypeService;
        this.indexItemService = indexItemService;
        this.userService = userService;

        this.mediaStorageService = mediaStorageService;
        this.sourceService = sourceService;

        this.eventPublisher = eventPublisher;
        this.vocabularyService = vocabularyService;
    }


    protected P getItemsPage(PageCoords pageCoords, boolean approved) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();

        Page<I> itemsPage = loadLatestItems(pageCoords, currentUser, approved);
        List<D> dtos = itemsPage.stream().map(this::prepareItemDto).collect(Collectors.toList());

        return wrapPage(itemsPage, dtos);
    }


    protected D getItemVersion(String persistentId, Long versionId) {
        I item = loadItemVersion(persistentId, versionId);

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!itemVisibilityService.hasAccessToVersion(item, currentUser)) {
            throw new AccessDeniedException(
                    String.format("User is not authorized to access the given item version with id %s (version id: %d)",
                            persistentId, versionId));
        }

        return prepareItemDto(item);
    }


    protected D getLatestItem(String persistentId, boolean draft, boolean approved, boolean redirect) {
        if (draft) {
            I itemDraft = loadItemDraftForCurrentUser(persistentId);
            return prepareItemDto(itemDraft);
        }

        if (redirect && isMerged(persistentId)) {
            I item = loadLatestItemOrRedirect(persistentId);
            return prepareItemDto(item);
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
        return createOrUpdateItemVersion(itemCore, null, draft, true, false);
    }


    protected void checkIfMergeIsPossible(List<String> persistentIdsToMerge)
            throws ItemIsAlreadyMergedException {
        for (String persistentIdToMerge : persistentIdsToMerge) {
            Optional<VersionedItem> versionedItem = versionedItemRepository.findByMergedWithPersistentId(
                    persistentIdToMerge);
            if (versionedItem.isPresent()) {
                throw new ItemIsAlreadyMergedException(persistentIdToMerge, versionedItem.get().getPersistentId());
            }
        }
    }


    protected I mergeItem(String persistentId, List<String> persistentIdsToMerge) {
        I mergedItem = loadCurrentItem(persistentId);
        // The merged item does not come from any source. Sources are only in the original items and
        // are available via API: GET /api/{category}/{persistentId}/sources
        mergedItem.setSource(null);
        mergedItem.setSourceItemId(null);
        mergedItem.getVersionedItem().setMergedWith(new ArrayList<>());

        for (String persistentIdToMerge : persistentIdsToMerge) {
            VersionedItem versionedItem = versionedItemRepository.getOne(persistentIdToMerge);
            I prevItem = (I) versionedItem.getCurrentVersion();
            prevItem.setStatus(ItemStatus.DEPRECATED);
            mergedItem.getVersionedItem().addMergedWith(versionedItem);
            itemRepository.save(prevItem);
            versionedItemRepository.save(versionedItem);
        }

        versionedItemRepository.save(mergedItem.getVersionedItem());
        itemRepository.save(mergedItem);

        eventPublisher.publishEvent(new ItemsMergedEvent(persistentId, persistentIdsToMerge));

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

    protected I updateItem(String persistentId, C itemCore, boolean draft, boolean approved)
            throws VersionNotChangedException {
        I currentItem = loadItemForCurrentUser(persistentId);
        ComparisonResult comparisonResult = ComparisonResult.UPDATED;
        if (!draft && currentItem.getStatus() != ItemStatus.DRAFT &&
                !(approved && LoggedInUserHolder.getLoggedInUser() != null &&
                        !LoggedInUserHolder.getLoggedInUser().isContributor() &&
                        currentItem.getStatus() != ItemStatus.APPROVED)) {
            comparisonResult = recognizePotentialChanges(currentItem, (ItemCore) itemCore);
            if (comparisonResult == ComparisonResult.UNMODIFIED) {
                throw new VersionNotChangedException();
            }
        }
        return createOrUpdateItemVersion(itemCore, currentItem, draft, approved,
                comparisonResult == ComparisonResult.CONFLICT);
    }


    protected ComparisonResult recognizePotentialChanges(I currentItem, ItemCore itemCore) {
        ItemDto currentItemDto = ItemsComparator.toDto(currentItem);
        currentItemDto.setRelatedItems(itemRelatedItemService.getItemRelatedItems(currentItem));
        complete(currentItemDto, currentItem);
        ItemDto itemDtoFromSource = null;
        if (itemCore.getSource() != null && itemCore.getSource().getId() != null
                && itemCore.getSourceItemId() != null) {
            Item itemFromSource = getLastItemBySource(currentItem, itemCore.getSource().getId(),
                    itemCore.getSourceItemId());
            if (itemFromSource != null) {
                itemDtoFromSource = ItemsComparator.toDtoSource(itemFromSource);
                itemDtoFromSource.setRelatedItems(itemRelatedItemService.getItemRelatedItems(itemFromSource));
                complete(itemDtoFromSource, itemFromSource);
            }
        }
        ItemDifferencesCore currentItemDifferences = ItemsComparator.differentiateItems(itemCore, currentItemDto);
        if (itemDtoFromSource != null) {
            ItemDifferencesCore itemFromSourceDifferences = ItemsComparator.differentiateItems(itemCore,
                    itemDtoFromSource);
            if (itemFromSourceDifferences.isEqual()) {
                return ComparisonResult.UNMODIFIED;
            } else {
                if (currentItemDifferences.isEqual()) {
                    return ComparisonResult.UNMODIFIED;
                } else {
                    if (ItemsConflictComparator.isConflict(currentItemDifferences, itemFromSourceDifferences)) {
                        return ComparisonResult.CONFLICT;
                    } else {
                        return ComparisonResult.UPDATED;
                    }
                }
            }
        } else {
            if (currentItemDifferences.isEqual()) {
                return ComparisonResult.UNMODIFIED;
            } else {
                return ComparisonResult.UPDATED;
            }
        }
    }


    private Item getLastItemBySource(@NonNull I currentItem, @NonNull Long sourceId, @NonNull String sourceItemId) {
        List<Item> history = loadItemHistory(currentItem);
        for (Item historicalItem : history)
            if (historicalItem.getSource() != null) {
                if (sourceId.equals(historicalItem.getSource().getId()) && sourceItemId.equals(
                        historicalItem.getSourceItemId()) && historicalItem.getInformationContributor().isSystemContributor()) {
                    return historicalItem;
                }
            }
        return null;
    }


    protected I createOrUpdateItemVersion(C itemCore, I prevVersion, boolean draft, boolean approved,
                                          boolean conflict) {
        I newItem = prepareAndPushItemVersion(itemCore, prevVersion, draft, approved, conflict);
        indexItemService.indexItem(newItem);
        return newItem;
    }


    private I prepareAndPushItemVersion(C itemCore, I prevVersion, boolean draft, boolean approved, boolean conflict) {
        // If there exists a draft item (owned by current user) then it should be modified instead of the current item version
        if (prevVersion != null && prevVersion.getStatus().equals(ItemStatus.DRAFT)) {

            unlinkItemMedia(prevVersion);

            I version = modifyItem(itemCore, prevVersion);
            itemRelatedItemService.updateRelatedItems(itemCore.getRelatedItems(), prevVersion, null, true);

            if (!draft)
                commitItemDraft(version);

            linkItemMedia(version);

            return version;
        }

        I version = makeItemVersion(itemCore, prevVersion, conflict);

        version = saveVersionInHistory(version, prevVersion, draft, approved);

        itemRelatedItemService.updateRelatedItems(itemCore.getRelatedItems(), version, prevVersion, draft);

        return version;
    }


    private I saveVersionInHistory(I version, I prevVersion, boolean draft, boolean approved) {
        return saveVersionInHistory(version, prevVersion, draft, true, approved);
    }


    // Warning: important method! Do not change unless you know what you are doing!
    private I saveVersionInHistory(I version, I prevVersion, boolean draft, boolean changeStatus, boolean approved) {
        VersionedItem versionedItem = (prevVersion == null) ? createNewVersionedItem() : prevVersion.getVersionedItem();

        if (!versionedItem.isActive()) {
            throw new IllegalArgumentException(
                    String.format("Item with id %s has been deleted or merged, so adding a new version is prohibited",
                            versionedItem.getPersistentId()));
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

        if (Objects.nonNull(version.getThumbnail()) && version.getThumbnail().getItemMediaThumbnail()
                .equals(ItemMediaType.THUMBNAIL_ONLY)) {
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

        indexItemService.indexItem(item);

        return item;
    }


    protected I commitItemDraft(I version) {
        DraftItem draft = draftItemRepository.findByItemId(version.getId()).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("%s with id %s and version id %s cannot be committed as it is not a draft",
                                getItemTypeName(), version.getVersionedItem().getPersistentId(), version.getId())

                ));

        VersionedItem versionedItem = version.getVersionedItem();
        if (versionedItem.getStatus() == VersionedItemStatus.DELETED) {
            throw new IllegalArgumentException(
                    String.format("Cannot commit draft for the deleted/merged item with id %s",
                            versionedItem.getPersistentId()));
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

        indexItemService.indexItem(targetVersion);

        return targetVersion;
    }

    protected I revertItemVersion(String persistentId) {
        I currentVersion = loadCurrentItem(persistentId, false);

        currentVersion.setStatus(ItemStatus.APPROVED);
        currentVersion.getVersionedItem().setActive(true);
        currentVersion.getVersionedItem().setStatus(VersionedItemStatus.REVIEWED);

        indexItemService.indexItem(currentVersion);

        return currentVersion;
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
        indexItemService.indexItem(newItem);

        return newItem;
    }


    protected void deleteItem(String persistentId, boolean draft) {
        if (draft) {
            deleteItemDraft(persistentId);
        } else {
            setDeleteItem(persistentId, null);
        }
    }


    protected void deleteItem(String persistentId, long versionId) {
        I item = loadItemVersion(persistentId, versionId);
        if (item.getStatus() == ItemStatus.DRAFT) {
            User currentUser = LoggedInUserHolder.getLoggedInUser();
            if (currentUser.equals(item.getInformationContributor())) {
                deleteItemDraft(persistentId);
            } else {
                throw new AccessDeniedException(String.format(
                        "User is not authorized to access the given draft version with id %s (version id: %d)",
                        persistentId, versionId));
            }
        } else {
            setDeleteItem(persistentId, versionId);
        }
    }


    protected void setDeleteItem(String persistentId, Long versionId) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!currentUser.isModerator())
            throw new AccessDeniedException("Current user is not a moderator and is not allowed to remove items");

        I currentItem = loadCurrentItem(persistentId);
        I item = (versionId != null) ? loadItemVersion(persistentId, versionId) : currentItem;

        VersionedItem versionedItem = item.getVersionedItem();

        if (item.getId().equals(currentItem.getId())) {
            if (versionedItem.getStatus() == VersionedItemStatus.INGESTED
                    || versionedItem.getStatus() == VersionedItemStatus.SUGGESTED) {
                versionedItem.setStatus(VersionedItemStatus.REFUSED);
                item.setStatus(ItemStatus.DISAPPROVED);
            } else {
                versionedItem.setStatus(VersionedItemStatus.DELETED);
                versionedItem.setActive(false);
            }
        } else {
            if (item.getStatus() == ItemStatus.INGESTED || item.getStatus() == ItemStatus.SUGGESTED) {
                item.setStatus(ItemStatus.DISAPPROVED);
            } else if (item.getStatus() == ItemStatus.APPROVED) {
                item.setStatus(ItemStatus.DEPRECATED);
            }
        }

        if (item.getId().equals(currentItem.getId())) {
            indexItemService.removeItemVersions(item);
        }
    }


    protected void deleteItemDraft(String persistentId) {
        I draftItem = loadItemDraftForCurrentUser(persistentId);
        cleanupDraft(draftItem);
    }


    private void cleanupDraft(I draftItem) {
        VersionedItem versionedItem = draftItem.getVersionedItem();
        if (!draftItem.getStatus().equals(ItemStatus.DRAFT)) {
            throw new IllegalStateException(
                    String.format("Unexpected attempt of removing a non-draft item with id %s as a draft item",
                            versionedItem.getPersistentId()));
        }

        unlinkItemMedia(draftItem);

        draftItemRepository.deleteByItemId(draftItem.getId());
        getItemRepository().delete(draftItem);

        if (versionedItem.getStatus().equals(VersionedItemStatus.DRAFT))
            versionedItemRepository.delete(draftItem.getVersionedItem());
    }


    private void unlinkItemMedia(I version) {
        version.getMedia().stream().map(ItemMedia::getMediaId).forEach(mediaStorageService::removeMediaLink);
    }

    private void unlinkAllItemMedia(I version) {
        version.getMedia().stream().map(ItemMedia::getMediaId).forEach(mediaStorageService::removeMediaLink);
        version.setMedia(new ArrayList<>());
    }


    private void commitDraftRelations(DraftItem draftItem) {
        itemRelatedItemService.commitDraftRelations(draftItem);
    }


    private void completeItemDto(D dto, I item) {
        completeDto(dto, item);
    }


    private void completeDto(D dto, Item item) {
        complete(dto, item);
    }


    private void complete(ItemDto dto, Item item) {
        completeDtoProperties(dto);

        List<UUID> mediaIds = item.getMedia().stream().map(ItemMedia::getMediaId).collect(Collectors.toList());

        Optional.ofNullable(item.getThumbnail()).ifPresent(thumbnail -> mediaIds.add(thumbnail.getMediaId()));

        List<MediaDetails> listOfMediaWithThumbnail = mediaStorageService.getMediaDetails(mediaIds);

        Map<UUID, List<MediaDetails>> groupedById = listOfMediaWithThumbnail.stream().collect(Collectors.groupingBy(MediaDetails::getMediaId));

        for (int i = 0; i < item.getMedia().size(); ++i) {
            ItemMedia media = item.getMedia().get(i);
            getMediaDetails(groupedById, media).ifPresent(dto.getMedia().get(i)::setInfo);
        }

        ItemMedia thumbnail = item.getThumbnail();
        if (thumbnail != null) {
            getMediaDetails(groupedById, thumbnail).ifPresent(dto.getThumbnail()::setInfo);
        }
    }

    private static Optional<MediaDetails> getMediaDetails(Map<UUID, List<MediaDetails>> groupedById, ItemMedia media) {
        if (groupedById.get(media.getMediaId()) != null && !groupedById.get(media.getMediaId()).isEmpty()) {
            return Optional.of(groupedById.get(media.getMediaId()).get(0));
        }
        return Optional.empty();
    }


    private void completeDtoProperties(ItemDto dto) {
        Map<String, List<Vocabulary>> allowedVocabularies = vocabularyService.getAllowedVocabulariesForPropertyTypes(
                dto.getProperties().stream()
                        .map(PropertyDto::getType)
                        .map(PropertyTypeDto::getCode)
                        .collect(Collectors.toList()));


        List<PropertyDto> properties = dto.getProperties().stream()
                .map(property -> {
                    List<Vocabulary> vocabularies = allowedVocabularies.getOrDefault(property.getType().getCode(), Collections.emptyList());
                    if (!vocabularies.isEmpty()) {
                        property.getType().setAllowedVocabularies(
                                VocabularyBasicMapper.INSTANCE.toDto(vocabularies));
                    }
                    return property;
                })
                .collect(Collectors.toList());

        dto.setProperties(properties);
    }

    protected List<ItemExtBasicDto> getItemHistory(String persistentId, Long versionId) {
        I item = loadItemVersion(persistentId, versionId);

        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (!itemVisibilityService.hasAccessToVersion(item, currentUser)) {
            throw new AccessDeniedException(
                    String.format("User is not authorized to access the given item version with id %s (version id: %d)",
                            persistentId, versionId));
        }
        return loadItemHistory(item).stream().map(ItemExtBasicConverter::convertItem).collect(Collectors.toList());
    }


    private List<Item> loadItemHistory(Item item) {
        List<Long> itemsIds = itemRepository.findItemsHistory(item.getPersistentId(), item.getId());
        return itemsIds.stream().map(id -> itemRepository.findById(id).get()).collect(Collectors.toList());
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

        // The merged item does not come from any source. Sources are only in the original items and
        // are available via API: GET /api/{category}/{persistentId}/sources
        finalDto.setSource(null);
        finalDto.setSourceItemId(null);

        finalDto.setRelatedItems(itemRelatedItemService.getItemRelatedItems(finalItem));
        completeItemDto(finalDto, finalItem);

        for (int i = 0; i < mergeList.size(); i++) {

            Optional<Item> toMergeHolder = itemRepository.findCurrentActiveVersion(mergeList.get(i));
            if (toMergeHolder.isEmpty())
                continue;
            Item toMerge = toMergeHolder.get();
            D toMergeDto = convertToDto(toMerge);
            itemDtoList.add(toMergeDto);

            itemDtoList.get(i).setRelatedItems(itemRelatedItemService.getItemRelatedItems(toMerge));
            completeDto(itemDtoList.get(i), toMerge);

            if (StringUtils.isNotBlank(itemDtoList.get(i).getDescription())) {
                if (StringUtils.isBlank(finalDto.getDescription()))
                    finalDto.setDescription(itemDtoList.get(i).getDescription());
                else if (!finalDto.getDescription().contains(itemDtoList.get(i).getDescription()))
                    finalDto.setDescription(finalDto.getDescription() + " / " + itemDtoList.get(i).getDescription());
            }

            if (StringUtils.isNotBlank(itemDtoList.get(i).getLabel())) {
                if (StringUtils.isBlank(finalDto.getLabel()))
                    finalDto.setLabel(itemDtoList.get(i).getLabel());
                else if (!finalDto.getLabel().contains(itemDtoList.get(i).getLabel()))
                    finalDto.setLabel(finalDto.getLabel() + " / " + itemDtoList.get(i).getLabel());
            }

            if (StringUtils.isNotBlank(itemDtoList.get(i).getVersion())) {
                if (StringUtils.isBlank(finalDto.getVersion()))
                    finalDto.setVersion(itemDtoList.get(i).getVersion());
                else if (!finalDto.getVersion().contains(itemDtoList.get(i).getVersion()))
                    finalDto.setVersion(finalDto.getVersion() + " / " + itemDtoList.get(i).getVersion());
            }

            for (ItemContributorDto itemContributor : itemDtoList.get(i).getContributors()) {
                if (!finalDto.getContributors().contains(itemContributor))
                    finalDto.getContributors().add(itemContributor);
            }

            for (PropertyDto property : itemDtoList.get(i).getProperties()) {
                if (!finalDto.getProperties().contains(property))
                    finalDto.getProperties().add(property);
            }

            for (ItemExternalIdDto itemExternalId : itemDtoList.get(i).getExternalIds()) {
                if (!finalDto.getExternalIds().contains(itemExternalId))
                    finalDto.getExternalIds().add(itemExternalId);
            }

            for (String accessibleAt : itemDtoList.get(i).getAccessibleAt()) {
                if (!finalDto.getAccessibleAt().contains(accessibleAt))
                    finalDto.getAccessibleAt().add(accessibleAt);
            }

            for (RelatedItemDto relatedItem : itemDtoList.get(i).getRelatedItems()) {
                if (!finalDto.getRelatedItems().contains(relatedItem))
                    finalDto.getRelatedItems().add(relatedItem);
            }

            for (ItemMediaDto itemMedia : itemDtoList.get(i).getMedia()) {
                if (!finalDto.getMedia().contains(itemMedia))
                    finalDto.getMedia().add(itemMedia);
            }
        }

        return finalDto;
    }


    public List<SourceDto> getAllSources(String persistentId) {
        return sourceService.getSourcesOfItem(persistentId);
    }


    private I makeItemVersion(C itemCore, I prevItem, boolean conflict) {
        return makeItem(itemCore, prevItem, conflict);
    }


    private I makeItemVersionCopy(I item) {
        return makeItemCopy(item);
    }


    protected I saveItemVersion(I item) {
        return getItemRepository().save(item);
    }


    protected ItemsDifferencesDto getDifferences(String persistentId, Long versionId, String otherPersistentId,
                                                 Long otherVersionId) {
        I item;
        if (Objects.isNull(versionId))
            item = loadLatestItem(persistentId);
        else
            item = loadItemVersionForCurrentUser(persistentId, versionId);

        ItemDto itemDto = ItemsComparator.toDto(item);
        itemDto.setRelatedItems(itemRelatedItemService.getItemRelatedItems(item));
        complete(itemDto, item);

        Optional<Item> otherHolder;
        if (Objects.isNull(otherVersionId))
            otherHolder = itemRepository.findCurrentActiveVersion(otherPersistentId);
        else
            otherHolder = itemRepository.findByVersionedItemPersistentIdAndId(otherPersistentId, otherVersionId);

        Item other = otherHolder.orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to find an item with id %s and version id %d", persistentId, versionId)));

        if (!itemVisibilityService.hasAccessToVersion(other, LoggedInUserHolder.getLoggedInUser())) {
            throw new AccessDeniedException(
                    String.format("User is not authorised to retrieve version %d of item %s.", otherVersionId,
                            otherPersistentId));
        }

        ItemDto otherDto = ItemsComparator.toDto(other);
        otherDto.setRelatedItems(itemRelatedItemService.getItemRelatedItems(other));
        complete(otherDto, other);

        return ItemsComparator.differentiateItems(itemDto, otherDto);
    }

    protected ItemsDifferencesDto differentiateComposedOf(WorkflowDto workflowDto, WorkflowDto otherWorkflowDto, ItemsDifferencesDto differences) {
        return ItemsComparator.differentiateComposedOf(workflowDto, otherWorkflowDto, differences);
    }

    protected abstract I makeItem(C itemCore, I prevItem, boolean conflict);

    protected abstract I modifyItem(C itemCore, I item);

    protected abstract I makeItemCopy(I item);

    protected abstract P wrapPage(Page<I> resultsPage, List<D> convertedDtos);

    protected abstract D convertItemToDto(I item);

    protected abstract D convertToDto(Item item);

}
