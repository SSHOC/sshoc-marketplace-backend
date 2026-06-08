package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.items.event.ItemsMergedEvent;
import eu.sshopencloud.marketplace.services.sources.event.SourceChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;

/**
 * Service responsible to handling events fired in different parts of the application
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndexItemEventsHandlerService {

    private final SolrClient solrClient;
    private final ItemRepository itemRepository;
    private final IndexItemService indexItemService;

    @Async
    @TransactionalEventListener(classes = {ActorChangedEvent.class}, phase = TransactionPhase.AFTER_COMMIT)
    public void handleChangedActor(ActorChangedEvent event) {
        if (event.isDeleted()) {
            indexItemService.reindexItems();
        } else {
            for (Item item : itemRepository.findByContributorActorId(event.getId())) {
                indexItemService.indexItem(item);
            }
        }
    }

    @Async
    @TransactionalEventListener(classes = {SourceChangedEvent.class}, phase = TransactionPhase.AFTER_COMMIT)
    public void handleChangedSource(SourceChangedEvent event) {
        if (event.isDeleted()) {
            indexItemService.reindexItems();
        } else {
            for (Item item : itemRepository.findBySourceId(event.getId())) {
                indexItemService.indexItem(item);
            }
        }
    }

    @Async
    @TransactionalEventListener(classes = {ItemsMergedEvent.class}, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMergedEvent(ItemsMergedEvent event) {
        for (String persistentId : event.getPersistentIdsToMerge()) {
            deleteByPersistentId(persistentId);
        }
        for (Item item : itemRepository.findByVersionedItemPersistentId(event.getNewPersistentId())) {
            indexItemService.indexItem(item);
        }
    }

    private void deleteByPersistentId(String persistentId) {
        try {
            solrClient.deleteByQuery(IndexItem.COLLECTION_NAME, IndexItem.PERSISTENT_ID_FIELD + ":" + persistentId);
            solrClient.commit(IndexItem.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
