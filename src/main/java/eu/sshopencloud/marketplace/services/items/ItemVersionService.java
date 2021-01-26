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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;


@RequiredArgsConstructor
abstract class ItemVersionService<I extends Item> {

    private final VersionedItemRepository versionedItemRepository;
    private final ItemVisibilityService itemVisibilityService;


    protected Page<I> loadLatestItems(PageCoords pageCoords, User user, boolean approved) {
        PageRequest pageRequest = PageRequest.of(
                pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))
        );

        ItemVersionRepository<I> itemRepository = getItemRepository();

        if (!approved || user == null || !user.isContributor())
            return itemRepository.findAllLatestApprovedItems(pageRequest);

        if (user.isModerator())
            itemRepository.findAllLatestItems(pageRequest);

        return getItemRepository().findUserLatestItems(user, pageRequest);
    }

    /**
     * Loads the most recent item for presentation purposes i.e. an approved item
     */
    protected I loadLatestItem(String persistentId) {
        return tryLoadLatestItem(persistentId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find latest approved %s with id %s",
                                        getItemTypeName(), persistentId
                                )
                        )
                );
    }

    protected Optional<I> tryLoadLatestItem(String persistentId) {
        return getItemRepository().findLatestItem(persistentId);
    }

    /**
     * Loads the most recent item for update. Does not necessarily need to be approved
     * For the internal use only, as this method does not validate user access privileges
     */
    protected I loadCurrentItem(String persistentId) {
        // Here - why not to load VersionedItem, and then do getCurrentVersion() ?
        // Because getCurrentVersion() returns Item, and we want the generic item type I
        // Hence, there is a dedicated method in the repository - do not remove
        return getItemRepository().findCurrentVersion(persistentId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find current %s with id %s",
                                        getItemTypeName(), persistentId
                                )
                        )
                );
    }

    protected I loadLatestItemForCurrentUser(String persistentId, boolean authorize) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();

        if (currentUser == null) {
            if (authorize)
                throw new AccessDeniedException("Cannot access unapproved item for an unauthorized user");

            return loadLatestItem(persistentId);
        }

        I item = loadCurrentItem(persistentId);
        while (item != null) {
            if (itemVisibilityService.hasAccessToVersion(item, currentUser))
                return item;

            if (item.getStatus().equals(ItemStatus.DEPRECATED))
                break;

            item = (I) item.getPrevVersion();
        }

        throw new EntityNotFoundException(
                String.format("Unable to find latest %s with id %s", getItemTypeName(), persistentId)
        );
    }

    protected Optional<I> loadItemDraft(String persistentId, @NonNull User draftOwner) {
        if (!versionedItemRepository.existsById(persistentId)) {
            throw new EntityNotFoundException(
                    String.format("Unable to find draft %s with id %s", getItemTypeName(), persistentId)
            );
        }

        return getItemRepository().findDraftVersion(persistentId, draftOwner);
    }

    protected I loadItemDraftForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find draft %s with id %s for the authorized user",
                                        getItemTypeName(), persistentId
                                )
                        )
                );
    }

    protected Optional<I> resolveItemDraftForCurrentUser(String persistentId) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (currentUser == null)
            throw new AccessDeniedException("Cannot access draft item for an unauthorized user");

        return loadItemDraft(persistentId, currentUser);
    }

    protected I loadItemForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId)
                .orElseGet(() -> loadCurrentItem(persistentId));
    }

    protected I loadDraftOrLatestItemForCurrentUser(String persistentId) {
        return resolveItemDraftForCurrentUser(persistentId)
                .orElseGet(() -> loadLatestItemForCurrentUser(persistentId, false));
    }

    protected I loadItemVersion(String persistentId, long versionId) {
        return getItemRepository().findByVersionedItemPersistentIdAndId(persistentId, versionId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find %s with id %s and version id %d",
                                        getItemTypeName(), persistentId, versionId
                                )
                        )
                );
    }


    protected abstract ItemVersionRepository<I> getItemRepository();

    protected abstract String getItemTypeName();
}
