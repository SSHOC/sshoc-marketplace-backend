package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
abstract class ItemVersionService<I extends Item> {

    private final VersionedItemRepository versionedItemRepository;
    private final ItemVisibilityService itemVisibilityService;

    protected Page<I> loadLatestItems(PageCoords pageCoords, User user, boolean approved) {
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(),
                Sort.by(Sort.Order.asc("label")));

        ItemVersionRepository<I> itemRepository = getItemRepository();

        if (approved || user == null) {
            return itemRepository.findAllLatestApprovedItems(pageRequest);
        }

        if (user.isModerator()) {
            return itemRepository.findAllLatestItems(pageRequest);
        }

        return itemRepository.findUserLatestItems(user, pageRequest);
    }

    /**
     * Loads the most recent item for presentation purposes i.e. an approved item
     */
    protected I loadLatestItem(String persistentId) {
        return tryLoadLatestItem(persistentId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to find latest approved %s with id %s", getItemTypeName(), persistentId)));
    }

    protected Optional<I> tryLoadLatestItem(String persistentId) {
        return getItemRepository().findLatestItem(persistentId);
    }

    /**
     * Loads the most recent item for update. Does not need to be approved.
     * For the internal use only, as this method does not validate user access privileges
     * @param onlyActive if true seeks for active items only
     */
    protected I loadCurrentItem(String persistentId, boolean onlyActive) {
        // Here - why not to load VersionedItem, and then do getCurrentVersion() ?
        // Because getCurrentVersion() and getCurrentActiveVersion() returns Item, and we want the generic item type I
        // Hence, there is a dedicated method in the repository - do not remove
        return (onlyActive ? getItemRepository().findCurrentActiveVersion(
                persistentId) : getItemRepository().findCurrentVersion(persistentId)).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Unable to find current %s with id %s", getItemTypeName(), persistentId)));
    }

    /**
     * Loads the most recent item for update. Does not need to be approved, but needs to be active.
     * For the internal use only, as this method does not validate user access privileges
     */
    protected I loadCurrentItem(String persistentId) {
        return loadCurrentItem(persistentId, true);
    }


    protected I loadLatestItemForCurrentUser(String persistentId, boolean authorize) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();

        if (currentUser == null) {
            if (authorize) {
                throw new AccessDeniedException("Cannot access unapproved item for an unauthorized user");
            }

            return loadLatestItem(persistentId);
        }

        I item = loadCurrentItem(persistentId);
        while (item != null) {
            if (itemVisibilityService.hasAccessToVersion(item, currentUser)) {
                return item;
            }

            if (item.getStatus().equals(ItemStatus.DEPRECATED)) {
                break;
            }

            item = (I) item.getPrevVersion();
        }

        throw new EntityNotFoundException(
                String.format("Unable to find latest %s with id %s", getItemTypeName(), persistentId));
    }

    protected Optional<I> loadItemDraft(String persistentId, @NonNull User draftOwner) {
        if (!versionedItemRepository.existsById(persistentId)) {
            log.error("Exception " +
                    String.format("Unable to find draft %s with id %s", getItemTypeName(), persistentId));
        }

        return getItemRepository().findDraftVersion(persistentId, draftOwner);
    }

    protected I loadItemDraftForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to find draft %s with id %s for the authorized user", getItemTypeName(),
                        persistentId)));
    }

    protected Optional<I> resolveItemDraftForCurrentUser(String persistentId) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (currentUser == null) {
            throw new AccessDeniedException("Cannot access draft item for an unauthorized user");
        }

        return loadItemDraft(persistentId, currentUser);
    }

    protected I loadItemForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId).orElseGet(() -> loadCurrentItem(persistentId));
    }


    protected I loadDraftOrLatestItemForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId).orElseGet(
                () -> loadLatestItemForCurrentUser(persistentId, false));
    }


    protected I loadItemVersion(String persistentId, long versionId) {
        return getItemRepository().findByVersionedItemPersistentIdAndId(persistentId, versionId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Unable to find %s with id %s and version id %d", getItemTypeName(), persistentId,
                                versionId)));
    }

    protected I loadItemVersionForCurrentUser(String persistentId, long versionId) {
        I item = loadItemVersion(persistentId, versionId);
        if (itemVisibilityService.hasAccessToVersion(item, LoggedInUserHolder.getLoggedInUser())) {
            return item;
        }
        throw new AccessDeniedException(
                String.format("User is not authorised to retrieve version %d of item %s.", versionId, persistentId));
    }

    protected abstract ItemVersionRepository<I> getItemRepository();

    protected abstract String getItemTypeName();

    protected boolean isMerged(String persistentId) {
        return !getItemRepository().findIfMergedItem(persistentId).isEmpty();
    }

    protected I loadLatestItemOrRedirect(String persistentId) {
        return tryLoadLatestMergedItem(persistentId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to find merged %s with id %s", getItemTypeName(), persistentId)));
    }

    protected Optional<I> tryLoadLatestMergedItem(String persistentId) {

        String itemPersistentId = persistentId;
        while (isMerged(itemPersistentId)) {
            itemPersistentId = getItemRepository().findMergedWithPersistentId(itemPersistentId);
        }
        return getItemRepository().findCurrentActiveVersion(itemPersistentId);
    }
}
