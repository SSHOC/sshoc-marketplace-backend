package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.repositories.items.ItemVersionRepository;

import javax.persistence.EntityNotFoundException;


abstract class ItemVersionService<I extends Item> {

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
