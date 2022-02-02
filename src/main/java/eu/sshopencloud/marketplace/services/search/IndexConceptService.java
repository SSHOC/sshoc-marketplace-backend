package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.search.IndexConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
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
public class IndexConceptService {

    private final IndexConceptRepository indexConceptRepository;
    private final ConceptRepository conceptRepository;

    private final PropertyTypeService propertyTypeService;
    private final VocabularyRepository vocabularyRepository;


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
        log.debug("Before concept index.");
        clearConceptIndex();
        for (Vocabulary vocabulary : vocabularyRepository.findAll()) {
            indexConcepts(vocabulary);
        }
        log.debug("After concept index.");
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
