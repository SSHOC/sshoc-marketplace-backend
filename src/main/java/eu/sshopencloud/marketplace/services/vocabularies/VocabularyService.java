package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedConcepts;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedVocabularies;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyDto;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyMapper;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.projection.VocabularyBasicView;
import eu.sshopencloud.marketplace.services.vocabularies.rdf.RDFModelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;

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
        Page<VocabularyBasicView> vocabulariesPage = vocabularyRepository.findAllVocabulariesBasicBy(
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label")))
        );

        List<VocabularyBasicDto> vocabularies = vocabulariesPage.stream()
                .map(VocabularyBasicMapper.INSTANCE::toDtoBasic)
                .collect(Collectors.toList());

        return PaginatedVocabularies.builder().vocabularies(vocabularies)
                .count(vocabulariesPage.getContent().size()).hits(vocabulariesPage.getTotalElements())
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage())
                .pages(vocabulariesPage.getTotalPages())
                .build();
    }

    public VocabularyDto getVocabulary(String code, PageCoords conceptPageCoords) {
        Vocabulary vocabulary = vocabularyRepository.findById(code).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + code));

        VocabularyDto resultVocabulary = VocabularyMapper.INSTANCE.toDto(vocabulary);
        PaginatedConcepts conceptResults = conceptService.getConcepts(vocabulary.getCode(), conceptPageCoords);

        resultVocabulary.setConceptResults(conceptResults);

        return resultVocabulary;
    }

    public VocabularyBasicDto createUploadedVocabulary(MultipartFile vocabularyFile) throws IOException, VocabularyAlreadyExistsException {
        String vocabularyCode = FilenameUtils.getBaseName(vocabularyFile.getOriginalFilename());

        if (StringUtils.isBlank(vocabularyCode))
            throw new IllegalArgumentException("Invalid vocabulary code: file must contain name");

        try {
            Vocabulary newVocabulary = createVocabulary(vocabularyCode, vocabularyFile.getInputStream());
            return VocabularyBasicMapper.INSTANCE.toDto(newVocabulary);
        }
        catch (RDFParseException | UnsupportedRDFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid vocabulary file contents: %s", e.getMessage()), e);
        }
    }

    public VocabularyBasicDto updateUploadedVocabulary(String vocabularyCode, MultipartFile vocabularyFile)
            throws IOException, VocabularyDoesNotExistException {

        String fileVocabularyCode = FilenameUtils.getBaseName(vocabularyFile.getOriginalFilename());

        if (!vocabularyCode.equals(fileVocabularyCode))
            throw new IllegalArgumentException("Vocabulary code and file name does not match");

        try {
            Vocabulary vocabulary = updateVocabulary(vocabularyCode, vocabularyFile.getInputStream());
            return VocabularyBasicMapper.INSTANCE.toDto(vocabulary);
        }
        catch (RDFParseException | UnsupportedRDFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid vocabulary file contents: %s", e.getMessage()), e);
        }
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

    public void removeVocabulary(String vocabularyCode) {
        throw new UnsupportedOperationException("not implemented");
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
