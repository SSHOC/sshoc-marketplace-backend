package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexItemRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.repositories.sources.SourceRepository;
import eu.sshopencloud.marketplace.repositories.sources.projection.DetailedSourceView;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.items.event.ItemsMergedEvent;
import eu.sshopencloud.marketplace.services.sources.event.SourceChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexItemService {

    private final IndexItemRepository indexItemRepository;
    private final ItemRepository itemRepository;

    private final SearchItemRepository searchItemRepository;

    private final ItemRelatedItemService itemRelatedItemService;
    private final SourceRepository sourceRepository;


    public IndexItem indexItem(Item item) {

        if (!(item.isNewestVersion() || item.isProposedVersion()))
            return null;

        if (item.isNewestVersion())
            removeItemVersions(item);

        List<Map<String, Object>> results = sourceRepository.findDetailedSourcesOfItem(item.getPersistentId());
        List<DetailedSourceView> detailedSources = results.stream().map(DetailedSourceView::new).collect(Collectors.toList());

        IndexItem indexedItem = IndexConverter.convertItem(item, itemRelatedItemService.countAllRelatedItems(item),
                detailedSources);

        return indexItemRepository.save(indexedItem);
    }

    public IndexItem indexItemAfterReindex(Item item) {

        List<DetailedSourceView> detailedSources = sourceRepository.findDetailedSourcesOfItem(item.getPersistentId()).stream().map(DetailedSourceView::new).collect(Collectors.toList());

        return indexItemRepository.save(IndexConverter.convertItem(item, itemRelatedItemService.countAllRelatedItems(item),
                detailedSources));
    }

    public void reindexItems() {
        log.debug("Before item reindex.");
        clearItemIndex();
        for (Item item : itemRepository.findAllItemsFaster()) {
            indexItemAfterReindex(item);
        }
        log.debug("After item reindex.");
    }

    public void clearItemIndex() {
        indexItemRepository.deleteAll();
    }

    //Eliza
    public void removeItemVersions(Item item) {
        indexItemRepository.deleteByPersistentId(item.getPersistentId());
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
            indexItemRepository.deleteByPersistentId(persistentId);
        }
        for (Item item : itemRepository.findByVersionedItemPersistentId(event.getNewPersistentId())) {
            indexItem(item);
        }
    }

}
