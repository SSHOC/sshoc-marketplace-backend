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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptService conceptService;
    private final ConceptRelatedConceptService conceptRelatedConceptService;

    private final PropertyService propertyService;
    private final PropertyTypeService propertyTypeService;


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

        if (vocabularyRepository.existsById(vocabularyCode))
            throw new VocabularyAlreadyExistsException(vocabularyCode);

        return constructVocabularyAndSave(vocabularyCode, turtleInputStream);
    }

    public Vocabulary updateVocabulary(String vocabularyCode, InputStream turtleInputStream)
            throws VocabularyDoesNotExistException, IOException, RDFParseException, UnsupportedRDFormatException {

        Vocabulary oldVocabulary = vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new VocabularyDoesNotExistException(vocabularyCode));

        Vocabulary updatedVocabulary = constructVocabularyAndSave(vocabularyCode, turtleInputStream);

        List<Concept> conceptsToRemove = missingConcepts(oldVocabulary.getConcepts(), updatedVocabulary.getConcepts());
        removeConcepts(conceptsToRemove);

        return updatedVocabulary;
    }

    private List<Concept> missingConcepts(Collection<Concept> oldConcepts, Collection<Concept> newConcepts) {
        Set<String> newConceptCodes = newConcepts.stream().map(Concept::getCode).collect(Collectors.toSet());
        return oldConcepts.stream()
                .filter(concept -> !newConceptCodes.contains(concept.getCode()))
                .collect(Collectors.toList());
    }

    public void removeVocabulary(String vocabularyCode) throws VocabularyDoesNotExistException {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new VocabularyDoesNotExistException(vocabularyCode));

        removeConcepts(vocabulary.getConcepts());

        propertyTypeService.removePropertyTypesAssociations(vocabularyCode);
        vocabularyRepository.delete(vocabulary);
    }

    private void removeConcepts(List<Concept> concepts) {
        propertyService.removePropertiesWithConcepts(concepts);
        conceptService.removeConcepts(concepts);
    }

    private Vocabulary constructVocabularyAndSave(String vocabularyCode, InputStream turtleInputStream)
            throws IOException, RDFParseException, UnsupportedRDFormatException {

        Model rdfModel = Rio.parse(turtleInputStream, "", RDFFormat.TURTLE);         // TODO own exceptions

        Vocabulary vocabulary = RDFModelParser.createVocabulary(vocabularyCode, rdfModel);

        Map<String, Concept> conceptMap = RDFModelParser.createConcepts(rdfModel, vocabulary);
        vocabulary.setConcepts(new ArrayList<>(conceptMap.values()));

        vocabularyRepository.save(vocabulary);

        List<ConceptRelatedConcept> conceptRelatedConcepts = RDFModelParser.createConceptRelatedConcepts(conceptMap, rdfModel);
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        return vocabulary;
    }
}
