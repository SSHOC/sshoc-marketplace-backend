package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.items.VersionedItem;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;
import eu.sshopencloud.marketplace.repositories.items.VersionedItemRepository;
import eu.sshopencloud.marketplace.services.auth.LoggedInUserHolder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;


@RequiredArgsConstructor
abstract class ItemVersionService<I extends Item> {

    private final VersionedItemRepository versionedItemRepository;


    /**
     * Loads the most recent item for presentation purposes i.e. an approved item
     */
    protected I loadLatestItem(String persistentId) {
        return getItemRepository().findByVersionedItemPersistentIdAndStatus(persistentId, ItemStatus.REVIEWED)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format(
                                        "Unable to find latest approved %s with id %s",
                                        getItemTypeName(), persistentId
                                )
                        )
                );
    }

    /**
     * Loads the most recent item for update. Does not necessarily need to be approved
     */
    protected I loadCurrentItem(String persistentId) {
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

    protected Optional<I> loadItemDraft(String persistentId, @NonNull User draftOwner) {
        if (!versionedItemRepository.existsById(persistentId)) {
            throw new EntityNotFoundException(
                    String.format("Unable to find draft %s with id %s", getItemTypeName(), persistentId)
            );
        }

        return getItemRepository().findDraftVersion(persistentId, draftOwner);

    }

    protected Optional<I> loadItemDraftForCurrentUser(String persistentId) {
        User currentUser = LoggedInUserHolder.getLoggedInUser();
        if (currentUser == null)
            throw new AccessDeniedException("Cannot access draft item for an unauthorized user");

        return loadItemDraft(persistentId, currentUser);
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
