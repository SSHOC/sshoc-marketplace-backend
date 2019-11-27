package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.vocabularies.rdf.RDFModelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptRepository conceptRepository;

    private final ConceptService conceptService;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public PaginatedVocabularies getVocabularies(int page, int perpage) {
        Page<Vocabulary> vocabularies = vocabularyRepository.findAll(PageRequest.of(page - 1, perpage, new Sort(Sort.Direction.ASC, "label")));
        for (Vocabulary vocabulary: vocabularies) {
            complete(vocabulary);
        }

        return PaginatedVocabularies.builder().vocabularies(vocabularies.getContent())
                .count(vocabularies.getContent().size()).hits(vocabularies.getTotalElements()).page(page).perpage(perpage).pages(vocabularies.getTotalPages())
                .build();
    }

    public Vocabulary getVocabulary(String code) {
        Optional<Vocabulary> vocabulary = vocabularyRepository.findById(code);
        if (!vocabulary.isPresent()) {
            throw new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + code);
        }
        return complete(vocabulary.get());
    }

    private Vocabulary complete(Vocabulary vocabulary) {
        List<Concept> concepts = new ArrayList<Concept>();
        for (Concept concept: conceptService.getConcepts(vocabulary.getCode())) {
            Concept conceptWithoutVocabulary = ConceptConverter.convertWithoutVocabulary(concept);
            conceptWithoutVocabulary.setRelatedConcepts(conceptRelatedConceptService.getConceptRelatedConcepts(concept.getCode(), vocabulary.getCode()));
            concepts.add(conceptWithoutVocabulary);
        }
        vocabulary.setConcepts(concepts);
        return vocabulary;
    }


    public Vocabulary createVocabulary(String vocabularyCode, InputStream turtleInputStream)
            throws VocabularyAlreadyExistsException, IOException, RDFParseException, UnsupportedRDFormatException {
        if (vocabularyRepository.existsById(vocabularyCode)) {
            throw new VocabularyAlreadyExistsException(vocabularyCode);
        }

        Model rdfModel = Rio.parse(turtleInputStream, "", RDFFormat.TURTLE);         // TODO own exceptions

        Vocabulary vocabulary = RDFModelParser.createVocabulary(vocabularyCode, rdfModel);
        vocabularyRepository.saveAndFlush(vocabulary);

        Map<String, Concept> conceptMap = RDFModelParser.createConcepts(rdfModel, vocabulary);
        RDFModelParser.completeConcepts(conceptMap, rdfModel);
        List<Concept> concepts = conceptRepository.saveAll(conceptMap.values());
        vocabulary.setConcepts(concepts);

        List<ConceptRelatedConcept> conceptRelatedConcepts = RDFModelParser.createConceptRelatedConcepts(conceptMap, rdfModel);
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        return vocabulary;
    }


}
