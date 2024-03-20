package eu.sshopencloud.marketplace.services.search;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.vocabularies.PropertyTypeService;
import eu.sshopencloud.marketplace.services.vocabularies.event.VocabulariesChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndexConceptService {
    private final SolrClient solrClient;

    private final ConceptRepository conceptRepository;

    private final PropertyTypeService propertyTypeService;
    private final VocabularyRepository vocabularyRepository;


    public void indexConcept(Concept concept, Vocabulary vocabulary) {
        List<PropertyType> propertyTypes = propertyTypeService.getAllowedPropertyTypesForVocabulary(vocabulary);
        try {
            solrClient.add(IndexConcept.COLLECTION_NAME, IndexConverter.covertConcept(concept, vocabulary, propertyTypes));
            solrClient.commit(IndexConcept.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeConcept(Concept concept, String vocabularyCode) {
        String conceptId = vocabularyCode + "-" + concept.getCode();
        try {
            solrClient.deleteById(IndexConcept.COLLECTION_NAME, conceptId);
            solrClient.commit(IndexConcept.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void indexConcepts(Vocabulary vocabulary) {
        List<PropertyType> propertyTypes = propertyTypeService.getAllowedPropertyTypesForVocabulary(vocabulary);
        if (!propertyTypes.isEmpty()) {
            log.debug("indexing " + vocabulary.getCode() + " vocabulary concepts");
            List<SolrInputDocument> indexConcepts =  conceptRepository.findByVocabularyCode(vocabulary.getCode())
                    .stream()
                    .map(concept -> IndexConverter.covertConcept(concept, vocabulary, propertyTypes))
                    .collect(Collectors.toList());

            try {
                solrClient.add(IndexConcept.COLLECTION_NAME, indexConcepts);
                solrClient.commit(IndexConcept.COLLECTION_NAME);
            } catch (SolrServerException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.debug("vocabulary " + vocabulary.getCode() + " has no property type so no concepts are indexed");
        }
    }

    public void removeConcepts(Vocabulary vocabulary) {
        List<String> indexConcepts =  conceptRepository.findByVocabularyCode(vocabulary.getCode())
                .stream()
                .map(concept -> IndexConverter.covertConcept(concept, vocabulary, Collections.emptyList()))
                .map(sid -> sid.getFieldValue(IndexConcept.ID_FIELD).toString())
                .collect(Collectors.toList());
        try {
            solrClient.deleteById(IndexConcept.COLLECTION_NAME, indexConcepts);
            solrClient.commit(IndexConcept.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearConceptIndex() {
        try {
            solrClient.deleteByQuery(IndexConcept.COLLECTION_NAME, "*:*");
            solrClient.commit(IndexConcept.COLLECTION_NAME);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
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
