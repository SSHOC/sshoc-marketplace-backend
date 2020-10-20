package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import eu.sshopencloud.marketplace.dto.items.ItemCore;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.mappers.items.ItemConverter;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.dto.items.ItemBasicDto;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.items.PersistentId;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
public abstract class ItemCrudService<I extends Item, D extends ItemDto, P extends PaginatedResult<D>, C extends ItemCore> {

    private final ItemRepository itemRepository;
    private final VersionedItemRepository versionedItemRepository;
    private final ItemRelatedItemService itemRelatedItemService;
    private final PropertyTypeService propertyTypeService;
    private final IndexService indexService;


    protected P getItemsPage(PageCoords pageCoords) {
        PageRequest pageRequest = PageRequest.of(
                pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))
        );

        Page<I> itemsPage = getItemRepository().findAllByStatus(ItemStatus.REVIEWED, pageRequest);
        List<D> dtos = itemsPage.stream()
                .map(this::convertItemToDto)
                .map(this::completeItem)
                .collect(Collectors.toList());

        return wrapPage(itemsPage, dtos);
    }

    protected D getItemVersion(String persistentId, Long versionId) {
        I item = getItemRepository().findByVersionedItemPersistentIdAndId(persistentId, versionId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with persistent id %s and version id %d",
                                        getItemTypeName(), persistentId, versionId
                                )
                        )
                );

        D dto = convertItemToDto(item);
        return completeItem(dto);
    }

    protected D getLatestItem(String persistentId) {
        I item = loadLatestItem(persistentId);
        D dto = convertItemToDto(item);

        return completeItem(dto);
    }

    private I loadLatestItem(String persistentId) {
        return getItemRepository().findByVersionedItemPersistentIdAndStatus(persistentId, ItemStatus.REVIEWED)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find latest %s with persistent id %s",
                                        getItemTypeName(), persistentId
                                )
                        )
                );
    }

    protected D createItem(C itemCore) {
        return createNewItemVersion(itemCore, null);
    }

    protected D updateItem(String persistentId, C itemCore) {
        I item = loadLatestItem(persistentId);
        return createNewItemVersion(itemCore, item);
    }

    private D createNewItemVersion(C itemCore, I prevVersion) {
        I newItem = makeItem(itemCore, prevVersion);
        newItem = getItemRepository().save(newItem);

        VersionedItem versionedItem = (prevVersion == null) ? createNewVersionedItem() : prevVersion.getVersionedItem();
        newItem.setVersionedItem(versionedItem);
        newItem.setStatus(ItemStatus.REVIEWED);

        if (prevVersion != null)
            prevVersion.setStatus(ItemStatus.DEPRECATED);

        indexService.indexItem(newItem);

        D dto = convertItemToDto(newItem);
        return completeItem(dto);
    }

    private VersionedItem createNewVersionedItem() {
        String id = resolveNewVersionedItemId();
        VersionedItem versionedItem = new VersionedItem(id);

        return versionedItemRepository.saveAndFlush(versionedItem);
    }

    private String resolveNewVersionedItemId() {
        String id = PersistentId.generated();
        int trials = 0;

        while (versionedItemRepository.existsById(id)) {
            trials++;
            if (trials >= 10)
                throw new RuntimeException("Could not assign an id for the versioned item");

            id = PersistentId.generated();
        }

        return id;
    }

    protected void deleteItem(String persistentId) {
        I item = loadLatestItem(persistentId);

        cleanupItem(item);

        if (ItemStatus.DRAFT.equals(item.getStatus())) {
            getItemRepository().delete(item);
        }
        else {
            item.setStatus(ItemStatus.DELETED);
        }
        // todo removing versioned item as well

        indexService.removeItem(item);
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

    @Transactional
    public boolean isNewestVersion(Item item) {
        Item nextVersion = itemRepository.findByPrevVersion(item);
        return (nextVersion == null);
    }

    private <T extends ItemDto> T completeItem(T item) {
        item.setRelatedItems(itemRelatedItemService.getItemRelatedItems(item.getId()));
        item.setOlderVersions(getOlderVersionsOfItem(item.getId()));
        item.setNewerVersions(getNewerVersionsOfItem(item.getId()));

        for (PropertyDto property : item.getProperties()) {
            propertyTypeService.completePropertyType(property.getType());
        }

        return item;
    }

    private void cleanupItem(Item item) {
        itemRelatedItemService.deleteRelationsForItem(item);
        skipItemInUpdateHistory(item);
    }

    private void skipItemInUpdateHistory(Item item) {
        Item nextVersion = itemRepository.findByPrevVersion(item);
        if (nextVersion == null)
            return;

        nextVersion.setPrevVersion(item.getPrevVersion());
    }

    protected abstract ItemVersionRepository<I> getItemRepository();

    protected abstract I makeItem(C itemCore, I prevItem);
    protected abstract P wrapPage(Page<I> resultsPage, List<D> convertedDtos);

    protected abstract D convertItemToDto(I item);

    protected abstract String getItemTypeName();
}
