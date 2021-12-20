package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyMapper;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.projection.VocabularyBasicView;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.rdf.RDFModelParser;
import eu.sshopencloud.marketplace.services.vocabularies.rdf.RDFModelPrinter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptService conceptService;
    private final ConceptRelatedConceptService conceptRelatedConceptService;

    private final PropertyService propertyService;
    private final PropertyTypeService propertyTypeService;

    private final RDFModelPrinter rdfModelPrinter;

    public VocabularyService(VocabularyRepository vocabularyRepository,
                             ConceptService conceptService,
                             ConceptRelatedConceptService conceptRelatedConceptService,
                             PropertyService propertyService,
                             PropertyTypeService propertyTypeService,
                             RDFModelPrinter rdfModelPrinter) {

        this.vocabularyRepository = vocabularyRepository;
        this.conceptService = conceptService;
        this.conceptRelatedConceptService = conceptRelatedConceptService;
        this.propertyService = propertyService;
        this.propertyTypeService = propertyTypeService;
        this.rdfModelPrinter = rdfModelPrinter;
    }

    public PaginatedVocabularies getVocabularies(PageCoords pageCoords) {
        Page<VocabularyBasicView> vocabulariesPage = vocabularyRepository.findAllBasicBy(
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
        Vocabulary vocabulary = loadVocabulary(code);
        VocabularyDto resultVocabulary = VocabularyMapper.INSTANCE.toDto(vocabulary);
        PaginatedConcepts conceptResults = conceptService.getConcepts(vocabulary.getCode(), conceptPageCoords);

        resultVocabulary.setConceptResults(conceptResults);

        return resultVocabulary;
    }

    public Vocabulary loadVocabulary(String vocabularyCode) {
        return vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));
    }

    public VocabularyBasicDto createUploadedVocabulary(MultipartFile vocabularyFile, boolean closed) throws IOException, VocabularyAlreadyExistsException {
        String vocabularyCode = FilenameUtils.getBaseName(vocabularyFile.getOriginalFilename());

        if (StringUtils.isBlank(vocabularyCode))
            throw new IllegalArgumentException("Invalid vocabulary code: file must contain name");

        try {
            Vocabulary newVocabulary = createVocabulary(vocabularyCode, vocabularyFile.getInputStream(), closed);
            return VocabularyBasicMapper.INSTANCE.toDto(newVocabulary);
        } catch (RDFParseException | UnsupportedRDFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid vocabulary file contents: %s", e.getMessage()), e);
        }
    }

    public VocabularyBasicDto updateUploadedVocabulary(String vocabularyCode, MultipartFile vocabularyFile, boolean forceUpdate, boolean closed)
            throws IOException {

        String fileVocabularyCode = FilenameUtils.getBaseName(vocabularyFile.getOriginalFilename());

        if (!vocabularyCode.equals(fileVocabularyCode))
            throw new IllegalArgumentException("Vocabulary code and file name does not match");

        try {
            Vocabulary vocabulary = updateVocabulary(vocabularyCode, vocabularyFile.getInputStream(), forceUpdate, closed);
            return VocabularyBasicMapper.INSTANCE.toDto(vocabulary);
        } catch (RDFParseException | UnsupportedRDFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid vocabulary file contents: %s", e.getMessage()), e);
        }
    }

    public Vocabulary createVocabulary(String vocabularyCode, InputStream turtleInputStream, boolean closed)
            throws VocabularyAlreadyExistsException, IOException, RDFParseException, UnsupportedRDFormatException {

        if (vocabularyRepository.findById(vocabularyCode).isPresent())
            throw new VocabularyAlreadyExistsException(vocabularyCode);

        return constructVocabularyAndSave(vocabularyCode, turtleInputStream, closed);
    }

    public Vocabulary updateVocabulary(String vocabularyCode, InputStream turtleInputStream, boolean forceUpdate, boolean closed)
            throws IOException, RDFParseException, UnsupportedRDFormatException {

        Vocabulary oldVocabulary = vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));

        List<Concept> oldConcepts = new ArrayList<>(oldVocabulary.getConcepts());
        Vocabulary updatedVocabulary = constructVocabularyAndSave(vocabularyCode, turtleInputStream, closed);

        List<Concept> conceptsToRemove = missingConcepts(oldConcepts, updatedVocabulary.getConcepts());

        if (!forceUpdate && !conceptsToRemove.isEmpty() && propertyService.existPropertiesWithConcepts(conceptsToRemove)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot update the vocabulary with code '%s' since the operation would " +
                                    "remove concepts associated with existing properties. " +
                                    "Use force=true parameter to update the vocabulary " +
                                    "and remove properties associated with old concepts.",
                            vocabularyCode
                    )
            );
        }

        removeConceptsAndProperties(conceptsToRemove);

        return updatedVocabulary;
    }

    private List<Concept> missingConcepts(Collection<Concept> oldConcepts, Collection<Concept> newConcepts) {
        Set<String> newConceptCodes = newConcepts.stream().map(Concept::getCode).collect(Collectors.toSet());
        return oldConcepts.stream()
                .filter(concept -> !newConceptCodes.contains(concept.getCode()))
                .collect(Collectors.toList());
    }

    public void removeVocabulary(String vocabularyCode, boolean forceRemove) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));

        if (!forceRemove && propertyService.existPropertiesFromVocabulary(vocabularyCode)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot remove vocabulary '%s' since there already exist properties which value belongs to this vocabulary. " +
                                    "Use force=true parameter to remove the vocabulary and the associated properties and concepts as well.",
                            vocabulary.getLabel()
                    )
            );
        }

        removeConceptsAndProperties(vocabulary.getConcepts());

        propertyTypeService.removePropertyTypesAssociations(vocabularyCode);
        vocabularyRepository.delete(vocabulary);
    }

    private void removeConceptsAndProperties(List<Concept> concepts) {
        propertyService.removePropertiesWithConcepts(concepts);
        conceptService.removeConcepts(concepts);
    }

    private Vocabulary constructVocabularyAndSave(String vocabularyCode, InputStream turtleInputStream, boolean closed)
            throws IOException, RDFParseException, UnsupportedRDFormatException {

        Model rdfModel = Rio.parse(turtleInputStream, "", RDFFormat.TURTLE);         // TODO own exceptions

        Vocabulary vocabulary = RDFModelParser.createVocabulary(vocabularyCode, rdfModel);

        // TODO change possible candidate concepts and relations to the proper one
        Map<String, Concept> conceptMap = RDFModelParser.createConcepts(rdfModel, vocabulary);
        vocabulary.setConcepts(new ArrayList<>(conceptMap.values()));
        vocabulary.setClosed(closed);

        vocabularyRepository.save(vocabulary);

        List<ConceptRelatedConcept> conceptRelatedConcepts = RDFModelParser.createConceptRelatedConcepts(conceptMap, rdfModel);
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        return vocabulary;
    }

    @Transactional(readOnly = true)
    public void exportVocabulary(String vocabularyCode, OutputStream outputStream) throws IOException {
        Vocabulary vocabulary = loadVocabulary(vocabularyCode);
        List<Concept> concepts = conceptService.getConceptsList(vocabularyCode);
        concepts.sort(new ConceptComparator());

        rdfModelPrinter.createModel(vocabulary);

        concepts.forEach(
                concept -> rdfModelPrinter.addConceptToModel(vocabulary.getScheme(), concept, conceptRelatedConceptService.getConceptRelatedConcept(concept.getCode(), vocabularyCode))
        );

        rdfModelPrinter.generateInverseStatements();

        String namespacePrefix;
        if (vocabulary.getNamespaces().containsKey(""))
            namespacePrefix = "@prefix : <" + vocabulary.getNamespaces().get("") + "> .\n";
        else
            namespacePrefix = "@prefix : <" + vocabulary.getNamespace() + "> .\n";

        outputStream.write(namespacePrefix.getBytes(StandardCharsets.UTF_8));

        Rio.write(rdfModelPrinter.getModel(), outputStream, RDFFormat.TURTLE);
    }

    public VocabularyBasicDto changeClosedFlag(String vocabularyCode, boolean closed) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));
        vocabulary.setClosed(closed);
        vocabularyRepository.save(vocabulary);
        return VocabularyBasicMapper.INSTANCE.toDto(vocabulary);
    }

}
