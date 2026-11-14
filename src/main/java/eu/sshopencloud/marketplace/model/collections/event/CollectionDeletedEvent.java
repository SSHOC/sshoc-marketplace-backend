package eu.sshopencloud.marketplace.model.collections.event;

import java.util.UUID;

public record CollectionDeletedEvent(long collectionId, UUID thumbnailMediaId) {

}
