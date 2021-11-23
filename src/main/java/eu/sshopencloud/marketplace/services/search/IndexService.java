package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.actors.Actor;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.actors.ActorRepository;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexActorRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexConceptRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexItemRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.actors.event.ActorChangedEvent;
import eu.sshopencloud.marketplace.services.items.ItemRelatedItemService;
import eu.sshopencloud.marketplace.services.sources.event.SourceChangedEvent;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.event.VocabulariesChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexService {

    private final IndexItemRepository indexItemRepository;
    private final ItemRepository itemRepository;
    private final IndexConceptRepository indexConceptRepository;
    private final ConceptRepository conceptRepository;

    private final SearchItemRepository searchItemRepository;

    private final PropertyTypeService propertyTypeService;

    private final VocabularyRepository vocabularyRepository;

    private final IndexActorRepository indexActorRepository;
    private final ActorRepository actorRepository;

    private final ItemRelatedItemService itemRelatedItemService;


    public IndexItem indexItem(Item item) {
        if (// item.getCategory().equals(ItemCategory.STEP) ||
                        !(item.isNewestVersion() || item.isProposedVersion()))
            return null;

        if (item.isNewestVersion())
            removeItemVersions(item);

        IndexItem indexedItem = IndexConverter.convertItem(item, itemRelatedItemService.countAllRelatedItems(item));
        return indexItemRepository.save(indexedItem);
    }

    public void reindexItems() {
        clearItemIndex();
        for (Item item : itemRepository.findAll()) {
            indexItem(item);
        }
    }

    public void clearItemIndex() {
        indexItemRepository.deleteAll();
    }


    public void removeItemVersions(Item item) {
        indexItemRepository.deleteByPersistentId(item.getPersistentId());
    }


    public void rebuildAutocompleteIndex() {
        searchItemRepository.rebuildAutocompleteIndex();
    }


    public IndexActor indexActor(Actor actor) {
        IndexActor indexedActor = IndexConverter.covertActor(actor);
        return indexActorRepository.save(indexedActor);
    }

    public void reindexActors() {
        clearActorIndex();
        for (Actor actor : actorRepository.findAll()) {
            indexActor(actor);
        }
    }

    public void clearActorIndex() {
        indexActorRepository.deleteAll();
    }

    public void removeActor(Long actorId) {
        indexActorRepository.deleteById(actorId.toString());
    }



    public IndexConcept indexConcept(Concept concept, Vocabulary vocabulary) {
        List<PropertyType> propertyTypes = propertyTypeService.getAllowedPropertyTypesForVocabulary(vocabulary);
        IndexConcept indexedConcept= IndexConverter.covertConcept(concept, vocabulary, propertyTypes);
        return indexConceptRepository.save(indexedConcept);
    }

    public void removeConcept(Concept concept, String vocabularyCode) {
        String conceptId = vocabularyCode + "-" + concept.getCode();
        indexConceptRepository.deleteById(conceptId);
    }

    public List<IndexConcept> indexConcepts(Vocabulary vocabulary) {
        List<PropertyType> propertyTypes = propertyTypeService.getAllowedPropertyTypesForVocabulary(vocabulary);
        if (!propertyTypes.isEmpty()) {
            log.debug("indexing " + vocabulary.getCode() + " vocabulary concepts");
            List<IndexConcept> indexConcepts =  conceptRepository.findByVocabularyCode(vocabulary.getCode())
                    .stream()
                    .map(concept -> IndexConverter.covertConcept(concept, vocabulary, propertyTypes))
                    .collect(Collectors.toList());
            return (List<IndexConcept>) indexConceptRepository.saveAll(indexConcepts);
        } else {
            log.debug("vocabulary " + vocabulary.getCode() + " has no property type so no concepts are indexed");
            return Collections.emptyList();
        }
    }

    public void removeConcepts(Vocabulary vocabulary) {
        List<IndexConcept> indexConcepts =  conceptRepository.findByVocabularyCode(vocabulary.getCode())
                .stream()
                .map(concept -> IndexConverter.covertConcept(concept, vocabulary, Collections.emptyList()))
                .collect(Collectors.toList());
        indexConceptRepository.deleteAll(indexConcepts);
    }

    public void clearConceptIndex() {
        indexConceptRepository.deleteAll();
    }

    public void reindexConcepts() {
        clearConceptIndex();
        for (Vocabulary vocabulary : vocabularyRepository.findAll()) {
            indexConcepts(vocabulary);
        }
    }

    @EventListener
    public void handleChangedVocabularies(VocabulariesChangedEvent event) {
        List<Vocabulary> toReindex = event.getChangedVocabularies();
        toReindex.forEach(vocabulary -> {
            removeConcepts(vocabulary);
            indexConcepts(vocabulary);
        });
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

}
