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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Value("${marketplace.index.reindexItemsBatchSize}")
    private int reindexItemsBatchSize;


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

    public void indexItems(List<Item> items) {
        List<Map<String, Object>> sourcesOfItems = sourceRepository
                .findDetailedSourcesOfItems(items.stream().map(Item::getPersistentId).collect(Collectors.toList()));

        // group by persistentId
        Map<String, List<Map<String, Object>>> sourcesByItemsPIDs = sourcesOfItems
                .stream()
                .collect(Collectors.toMap(map -> (String) map.get(SourceRepository.PERSISTENT_ID_COLUMN_NAME), map -> {
                            List<Map<String, Object>> value = new ArrayList<>();
                            value.add(map);
                            return value;
                        }
                        , (a, b) -> {
                            a.addAll(b);
                            return a;
                        }
                ));

        // convert values in the map to list of DetailedSourceView
        Map<String, List<DetailedSourceView>> detailedSourcesByPIDs = convertValuesToDetailedSourceViewObjects(sourcesByItemsPIDs);

        Map<Long, Long> countOfRelatedByItemId = itemRelatedItemService.countAllRelatedItems(items.stream().map(Item::getId).collect(Collectors.toList()));

        List<SolrInputDocument> solrInputDocuments = items.stream()
                .map(item -> IndexConverter.convertItem(item, countOfRelatedByItemId.getOrDefault(item.getId(), 0L).intValue(), detailedSourcesByPIDs.getOrDefault(item.getPersistentId(), List.of())))
                .collect(Collectors.toList());

        try {
            solrClient.add(IndexItem.COLLECTION_NAME, solrInputDocuments);
            solrClient.commit(IndexItem.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull Map<String, List<DetailedSourceView>> convertValuesToDetailedSourceViewObjects(Map<String, List<Map<String, Object>>> sourcesByItemsPIDs) {
        Map<String, List<DetailedSourceView>> detailedSourcesByPIDs = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> sbpe: sourcesByItemsPIDs.entrySet()) {
            detailedSourcesByPIDs.compute(sbpe.getKey(), (k, v) -> {
                        List<DetailedSourceView> result = sbpe.getValue().stream().map(DetailedSourceView::new).collect(Collectors.toCollection(ArrayList::new));
                        if (v == null) {
                            return result;
                        }
                        v.addAll(result);
                        return v;
                    }
            );
        }
        return detailedSourcesByPIDs;
    }


    public void reindexItems() {
        log.debug("Before item reindex. Clearing index...");
        clearItemIndex();

        log.debug("Retrieving items to reindex...");
        List<Item> itemsToReindex = itemRepository.findAllItemsToReindex();

        long noOfItems = itemsToReindex.size();
        log.debug("{} items retrieved. Starting reindexing process using batches of {} items.", noOfItems, reindexItemsBatchSize);

        long noOfReindexedItems = 0;
        long noOfIteratedItems = 0;
        List<Item> batch = new ArrayList<>();
        for (Item item : itemsToReindex) {
            noOfIteratedItems++;
            batch.add(item);
            if (noOfIteratedItems % reindexItemsBatchSize == 0) {
                indexItems(batch);
                noOfReindexedItems += batch.size(); // size() should be equal to reindexItemsBatchSize
                batch.clear();
                log.debug("Reindexed {} of {} items", noOfReindexedItems, noOfItems);
            }
        }

        if (!batch.isEmpty()) {
            indexItems(batch);
            noOfReindexedItems += batch.size();
            batch.clear();
            log.debug("Reindexed {} of {} items", noOfReindexedItems, noOfItems);
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
