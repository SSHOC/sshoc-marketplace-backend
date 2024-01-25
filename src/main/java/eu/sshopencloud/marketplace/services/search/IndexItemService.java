package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.repositories.sources.projection.DetailedSourceView;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.event.ItemsMergedEvent;
import eu.sshopencloud.marketplace.services.sources.event.SourceChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexItemService {

    private final SolrClient solrClient;
    private final ItemRepository itemRepository;

    private final SearchItemRepository searchItemRepository;

    private final ItemRelatedItemService itemRelatedItemService;
    private final SourceRepository sourceRepository;


    public void indexItem(Item item) {

        if (!item.isNewestVersion() && !item.isProposedVersion()) {
            return;
        }

        if (item.isNewestVersion())
            removeItemVersions(item);

        List<Map<String, Object>> results = sourceRepository.findDetailedSourcesOfItem(item.getPersistentId());
        List<DetailedSourceView> detailedSources = results.stream().map(DetailedSourceView::new).collect(Collectors.toList());

        SolrInputDocument indexedItem = IndexConverter.convertItem(item, itemRelatedItemService.countAllRelatedItems(item),
                detailedSources);

        try {
            solrClient.add(IndexItem.COLLECTION_NAME, indexedItem);
            solrClient.commit(IndexItem.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void indexItemAfterReindex(Item item) {
        List<DetailedSourceView> detailedSources = sourceRepository.findDetailedSourcesOfItem(item.getPersistentId()).stream().map(DetailedSourceView::new).collect(Collectors.toList());

        try {
            solrClient.add(IndexItem.COLLECTION_NAME, IndexConverter.convertItem(item, itemRelatedItemService.countAllRelatedItems(item),
                    detailedSources));
            solrClient.commit(IndexItem.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void reindexItems() {
        log.debug("Before item reindex.");
        clearItemIndex();
        for (Item item : itemRepository.findAllItemsToReindex()) {
            indexItemAfterReindex(item);
        }
        log.debug("After item reindex.");
    }

    public void clearItemIndex() {
        try {
            solrClient.deleteByQuery(IndexItem.COLLECTION_NAME, "*:*");
            solrClient.commit(IndexItem.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void removeItemVersions(Item item) {
        deleteByPersistentId(item.getPersistentId());
        //indexItemRepository.deleteByPersistentId(item.getPersistentId());
    }

    private void deleteByPersistentId(String persistentId) {
        try {
            solrClient.deleteByQuery(IndexItem.COLLECTION_NAME, IndexItem.PERSISTENT_ID_FIELD + ":" + persistentId);
            solrClient.commit(IndexItem.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void rebuildAutocompleteIndex() {
        searchItemRepository.rebuildAutocompleteIndex();
    }


    @Async
    @TransactionalEventListener(classes = {SourceChangedEvent.class}, phase = TransactionPhase.AFTER_COMMIT)
    public void handleChangedSource(SourceChangedEvent event) {
        if (event.isDeleted()) {
            reindexItems();
        } else {
            for (Item item : itemRepository.findBySourceId(event.getId())) {
                indexItem(item);
            }
        }
    }

    @Async
    @TransactionalEventListener(classes = {ActorChangedEvent.class}, phase = TransactionPhase.AFTER_COMMIT)
    public void handleChangedActor(ActorChangedEvent event) {
        if (event.isDeleted()) {
            reindexItems();
        } else {
            for (Item item : itemRepository.findByContributorActorId(event.getId())) {
                indexItem(item);
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
            indexItem(item);
        }
    }

}
