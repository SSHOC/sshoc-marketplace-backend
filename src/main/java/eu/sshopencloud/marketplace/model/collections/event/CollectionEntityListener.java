package eu.sshopencloud.marketplace.model.collections.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import eu.sshopencloud.marketplace.model.collections.Collection;
import jakarta.persistence.PreRemove;

/**
 * Publishes a CollectionDeletedEvent when a collection is removed. The
 * callback fires on every deletion route (explicit deletes, cascades and
 * orphan removal alike). The event carries the thumbnail's media id because
 * once the deletion commits the row is gone and it can no longer be looked
 * up -- the actual cleanup is done by CollectionDeletedEventListener after
 * the transaction commits.
 */
@Component
public class CollectionEntityListener {

    private final ApplicationEventPublisher publisher;

    public CollectionEntityListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PreRemove
    public void onPreRemove(Collection collection) {
        publisher.publishEvent(new CollectionDeletedEvent(
                collection.getId(),
                collection.getThumbnail() != null
                        ? collection.getThumbnail().getThumbnailId()
                        : null));
    }
}
