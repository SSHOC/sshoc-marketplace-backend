package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.search.IndexConceptRepository;
import eu.sshopencloud.marketplace.repositories.search.IndexItemRepository;
import eu.sshopencloud.marketplace.services.items.ItemService;
import eu.sshopencloud.marketplace.services.vocabularies.ConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class IndexService {

    private final IndexItemRepository indexItemRepository;

    private final ItemService itemService;

    private final IndexConceptRepository indexConceptRepository;

    private final PropertyTypeService propertyTypeService;

    private final ConceptService conceptService;


    public IndexItem indexItem(Item item) {
        if (itemService.isNewestVersion(item)) {
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


    public List<IndexConcept> indexConcepts(Vocabulary vocabulary) {
        List<PropertyType> proopertyTypes = propertyTypeService.getAllowedPropertyTypesForVocabulary(vocabulary);
        List<IndexConcept> indexConcepts = conceptService.getConcepts(vocabulary.getCode()).stream()
                .map(concept -> IndexConverter.covertConcept(concept, vocabulary, proopertyTypes))
                .collect(Collectors.toList());;
        return (List<IndexConcept>) indexConceptRepository.saveAll(indexConcepts);
    }

    public void removeConcepts(Vocabulary vocabulary) {
        List<IndexConcept> indexConcepts = conceptService.getConcepts(vocabulary.getCode()).stream()
                .map(concept -> IndexConverter.covertConcept(concept, vocabulary, Collections.emptyList()))
                .collect(Collectors.toList());;
        indexConceptRepository.deleteAll(indexConcepts);
    }

    public void clearConceptIndex() {
        indexConceptRepository.deleteAll();
    }

}
