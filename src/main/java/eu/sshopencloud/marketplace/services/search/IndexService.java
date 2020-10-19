package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexConceptRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexItemRepository;
import eu.sshopencloud.marketplace.repositories.search.SearchItemRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.items.ItemCrudService;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.event.VocabulariesChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexService {

    private final IndexItemRepository indexItemRepository;

    private final SearchItemRepository searchItemRepository;

    private final ItemCrudService itemService;

    private final ItemRepository itemRepository;

    private final IndexConceptRepository indexConceptRepository;

    private final PropertyTypeService propertyTypeService;

    private final ConceptService conceptService;

    private final VocabularyRepository vocabularyRepository;


    public IndexItem indexItem(Item item) {
        if (!item.getCategory().equals(ItemCategory.STEP) && itemService.isNewestVersion(item)) {
            if (item.getPrevVersion() != null) {
                removeItem(item.getPrevVersion());
            }
            IndexItem indexItem = IndexConverter.covertItem(item);
            return indexItemRepository.save(indexItem);
        }
        return null;
    }

    public void removeItem(Item item) {
        indexItemRepository.deleteById(item.getId());
    }

    public void clearItemIndex() {
        indexItemRepository.deleteAll();
    }


    public void reindexItems() {
        clearItemIndex();
        for (Item item : itemRepository.findAll()) {
            indexItem(item);
        }
    }


    public List<IndexConcept> indexConcepts(Vocabulary vocabulary) {
        List<PropertyType> propertyTypes = propertyTypeService.getAllowedPropertyTypesForVocabulary(vocabulary);
        if (!propertyTypes.isEmpty()) {
            log.debug("indexing " + vocabulary.getCode() + " vocabulary concepts");
            List<IndexConcept> indexConcepts = conceptService.getConcepts(vocabulary.getCode()).stream()
                    .map(concept -> IndexConverter.covertConcept(concept, vocabulary, propertyTypes))
                    .collect(Collectors.toList());
            return (List<IndexConcept>) indexConceptRepository.saveAll(indexConcepts);
        } else {
            log.debug("vocabulary " + vocabulary.getCode() + " has no property type so no concepts are indexed");
            return Collections.emptyList();
        }
    }

    public void rebuildAutocompleteIndex() {
        searchItemRepository.rebuildAutocompleteIndex();
    }

    public void removeConcepts(Vocabulary vocabulary) {
        List<IndexConcept> indexConcepts = conceptService.getConcepts(vocabulary.getCode()).stream()
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
}
