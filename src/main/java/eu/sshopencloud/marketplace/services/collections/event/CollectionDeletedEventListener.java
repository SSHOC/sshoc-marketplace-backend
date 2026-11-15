package eu.sshopencloud.marketplace.services.collections.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import eu.sshopencloud.marketplace.domain.media.MediaStorageService;
import eu.sshopencloud.marketplace.services.search.IndexCollectionService;
import eu.sshopencloud.marketplace.model.collections.event.CollectionDeletedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Releases the media reference held by a deleted collection's thumbnail.
 *
 * The event is published by CollectionEntityListener (in the model layer) on
 * every deletion route -- API deletes, cascades from a deleted user, orphan
 * removal -- making this the single place where thumbnail media cleanup
 * happens.
 *
 * AFTER_COMMIT ensures the cleanup only runs if the deletion transaction
 * actually committed: if it rolls back nothing was deleted, so there is
 * nothing to clean up, and a failure here cannot fail the (already
 * successful) deletion.
 *
 * REQUIRES_NEW is essential: database access performed in an AFTER_COMMIT
 * listener joins the original transaction's resources, which have already
 * been committed, so without it the refcount decrement would silently never
 * be committed. See the
 * shouldDecrementMediaRefCountWhenCollectionIsDeletedViaCascade test in
 * CollectionControllerITCase, which proves the decrement is committed.
 */
@Slf4j
@Component
public class CollectionDeletedEventListener {

    private final MediaStorageService mediaStorageService;
    private final IndexCollectionService indexCollectionService;

    public CollectionDeletedEventListener(MediaStorageService mediaStorageService, IndexCollectionService indexCollectionService) {
        this.mediaStorageService = mediaStorageService;
        this.indexCollectionService = indexCollectionService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCollectionDeleted(CollectionDeletedEvent event) {
        if (event.thumbnailMediaId() != null) {
            try {
                mediaStorageService.removeMediaLink(event.thumbnailMediaId());
            } catch (Exception e) {
                log.error("Failed to remove media link for collection {}", event.collectionId(), e);
            }
        }

        try {
            indexCollectionService.removeFromIndex(event.collectionId());
        } catch (Exception e) {
            log.error("Failed to remove collection {} from the search index", event.collectionId(), e);
        }
    }
}
