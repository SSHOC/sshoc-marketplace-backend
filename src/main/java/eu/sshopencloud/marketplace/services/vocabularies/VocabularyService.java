package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.*;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyMapper;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.projection.VocabularyBasicView;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.rdf.RDFModelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.hibernate.boot.archive.scan.internal.StandardScanOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Throwable.class)
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    private final ConceptService conceptService;
    private final ConceptRelatedConceptService conceptRelatedConceptService;

    private final PropertyService propertyService;
    private final PropertyTypeService propertyTypeService;

    private final ConceptRelationService conceptRelationService;


    public PaginatedVocabularies getVocabularies(PageCoords pageCoords) {
        Page<VocabularyBasicView> vocabulariesPage = vocabularyRepository.findAllVocabulariesBasicBy(
                PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("label" )))
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

    public VocabularyBasicDto createUploadedVocabulary(MultipartFile vocabularyFile) throws IOException, VocabularyAlreadyExistsException {
        String vocabularyCode = FilenameUtils.getBaseName(vocabularyFile.getOriginalFilename());

        if (StringUtils.isBlank(vocabularyCode))
            throw new IllegalArgumentException("Invalid vocabulary code: file must contain name" );

        try {
            Vocabulary newVocabulary = createVocabulary(vocabularyCode, vocabularyFile.getInputStream());
            return VocabularyBasicMapper.INSTANCE.toDto(newVocabulary);
        } catch (RDFParseException | UnsupportedRDFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid vocabulary file contents: %s", e.getMessage()), e);
        }
    }

    public VocabularyBasicDto updateUploadedVocabulary(String vocabularyCode, MultipartFile vocabularyFile, boolean forceUpdate)
            throws IOException {

        String fileVocabularyCode = FilenameUtils.getBaseName(vocabularyFile.getOriginalFilename());

        if (!vocabularyCode.equals(fileVocabularyCode))
            throw new IllegalArgumentException("Vocabulary code and file name does not match" );

        try {
            Vocabulary vocabulary = updateVocabulary(vocabularyCode, vocabularyFile.getInputStream(), forceUpdate);
            return VocabularyBasicMapper.INSTANCE.toDto(vocabulary);
        } catch (RDFParseException | UnsupportedRDFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid vocabulary file contents: %s", e.getMessage()), e);
        }
    }

    public Vocabulary createVocabulary(String vocabularyCode, InputStream turtleInputStream)
            throws VocabularyAlreadyExistsException, IOException, RDFParseException, UnsupportedRDFormatException {

        if (vocabularyRepository.existsById(vocabularyCode))
            throw new VocabularyAlreadyExistsException(vocabularyCode);

        return constructVocabularyAndSave(vocabularyCode, turtleInputStream);
    }

    public Vocabulary updateVocabulary(String vocabularyCode, InputStream turtleInputStream, boolean forceUpdate)
            throws IOException, RDFParseException, UnsupportedRDFormatException {

        Vocabulary oldVocabulary = vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));

        List<Concept> oldConcepts = new ArrayList<>(oldVocabulary.getConcepts());
        Vocabulary updatedVocabulary = constructVocabularyAndSave(vocabularyCode, turtleInputStream);

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

    private Vocabulary constructVocabularyAndSave(String vocabularyCode, InputStream turtleInputStream)
            throws IOException, RDFParseException, UnsupportedRDFormatException {

        Model rdfModel = Rio.parse(turtleInputStream, "", RDFFormat.TURTLE);         // TODO own exceptions

        Vocabulary vocabulary = RDFModelParser.createVocabulary(vocabularyCode, rdfModel);

        // TODO change possible candidate concepts and relations to the proper one
        Map<String, Concept> conceptMap = RDFModelParser.createConcepts(rdfModel, vocabulary);
        vocabulary.setConcepts(new ArrayList<>(conceptMap.values()));

        vocabularyRepository.save(vocabulary);

        List<ConceptRelatedConcept> conceptRelatedConcepts = RDFModelParser.createConceptRelatedConcepts(conceptMap, rdfModel);
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        return vocabulary;
    }


    public String exportVocabulary(String vocabularyCode) throws IOException {
        Vocabulary vocabulary = loadVocabulary(vocabularyCode);
        List<Concept> concepts = conceptService.getConceptsList(vocabularyCode);

        List<ConceptRelation> conceptRelations = conceptRelationService.getConceptRelations();

     //   List<ConceptRelatedConcept> conceptRelatedConcepts = conceptRelatedConceptService.getRelatedConcepts();
        String mainSchema = vocabulary.getNamespace() + "Schema";

        Model model = RDFModelParser.createRDFModelR(vocabulary);
        model = RDFModelParser.generateConcepts(model, mainSchema, concepts);


        FileOutputStream out = new FileOutputStream("outputTutrle2.ttl" );
        try {
            Rio.write(model, out, RDFFormat.TURTLE);
        } finally {
            out.close();
        }

        try {
            insertStringInFile("@prefix : <" + vocabulary.getNamespace() + ">" );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public void insertStringInFile(String lineToBeInserted)
            throws Exception {

        ArrayList<String> list = (ArrayList<String>) Files.readAllLines(new File("C:\\Users\\109-DBCiPW-elik\\IdeaProjects\\SSHOC\\sshoc-marketplace-backend\\outputTutrle2.ttl" ).toPath(), Charset.defaultCharset());

        list.add(0, lineToBeInserted);

        Path write = Paths.get("C:\\Users\\109-DBCiPW-elik\\IdeaProjects\\SSHOC\\sshoc-marketplace-backend", "outputTutrle2.ttl" );

        Files.write(write, list, Charset.defaultCharset(), StandardOpenOption.WRITE);

    }

}
