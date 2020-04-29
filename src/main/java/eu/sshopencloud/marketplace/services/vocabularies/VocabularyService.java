package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.mappers.vocabularies.ConceptMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyMapper;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptRepository conceptRepository;

    private final ConceptService conceptService;

    private final ConceptRelatedConceptService conceptRelatedConceptService;

    public PaginatedVocabularies getVocabularies(PageCoords pageCoords) {
        Page<Vocabulary> vocabulariesPage = vocabularyRepository.findAll(PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label"))));
        List<VocabularyDto> vocabularies = vocabulariesPage.stream().map(VocabularyMapper.INSTANCE::toDto).map(this::completeVocabulary).collect(Collectors.toList());

        return PaginatedVocabularies.builder().vocabularies(vocabularies)
                .count(vocabulariesPage.getContent().size()).hits(vocabulariesPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(vocabulariesPage.getTotalPages())
                .build();
    }

    public VocabularyDto getVocabulary(String code) {
        Vocabulary vocabulary = vocabularyRepository.findById(code).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + code));
        return completeVocabulary(VocabularyMapper.INSTANCE.toDto(vocabulary));
    }

    public VocabularyDto completeVocabulary(VocabularyDto vocabulary) {
        vocabulary.setConcepts(
                conceptService.getConcepts(vocabulary.getCode()).stream().map(ConceptMapper.INSTANCE::toDto)
                .map(concept -> {
                    concept.setRelatedConcepts(conceptRelatedConceptService.getRelatedConcepts(concept.getCode(), vocabulary.getCode()));
                    return concept;
                    })
                .collect(Collectors.toList()));
        return vocabulary;
    }


    public Vocabulary createVocabulary(String vocabularyCode, InputStream turtleInputStream)
            throws VocabularyAlreadyExistsException, IOException, RDFParseException, UnsupportedRDFormatException {
        if (vocabularyRepository.existsById(vocabularyCode)) {
            throw new VocabularyAlreadyExistsException(vocabularyCode);
        }

        return constructVocabularyAndSave(vocabularyCode, turtleInputStream);
    }

    public Vocabulary updateVocabulary(String vocabularyCode, InputStream turtleInputStream)
            throws VocabularyDoesNotExistException, IOException, RDFParseException, UnsupportedRDFormatException {
        if (!vocabularyRepository.existsById(vocabularyCode)) {
            throw new VocabularyDoesNotExistException(vocabularyCode);
        }

        return constructVocabularyAndSave(vocabularyCode, turtleInputStream);
    }

    private Vocabulary constructVocabularyAndSave(String vocabularyCode, InputStream turtleInputStream)
            throws IOException, RDFParseException, UnsupportedRDFormatException {

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
