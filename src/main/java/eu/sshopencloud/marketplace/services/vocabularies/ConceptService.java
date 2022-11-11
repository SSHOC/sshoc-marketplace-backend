package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptCore;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedConcepts;
import eu.sshopencloud.marketplace.mappers.vocabularies.ConceptMapper;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.items.ItemRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.search.IndexConceptService;
import eu.sshopencloud.marketplace.services.vocabularies.exception.ConceptAlreadyExistsException;
import eu.sshopencloud.marketplace.services.vocabularies.exception.VocabularyIsClosedException;
import eu.sshopencloud.marketplace.validators.vocabularies.ConceptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptService {

    private final ConceptRepository conceptRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ConceptFactory conceptFactory;
    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;
    private final ConceptRelatedConceptDetachingRepository conceptRelatedConceptDetachingRepository;
    private final ConceptRelatedConceptService conceptRelatedConceptService;
    private final PropertyService propertyService;
    private final IndexConceptService indexConceptService;
    private final ItemRepository itemRepository;


    public PaginatedConcepts getConcepts(String vocabularyCode, PageCoords pageCoords) {
        PageRequest pageRequest = PageRequest.of(pageCoords.getPage() - 1, pageCoords.getPerpage(),
                Sort.by(Sort.Order.asc("ord")));

        Page<Concept> conceptsPage = conceptRepository.findByVocabularyCode(vocabularyCode, pageRequest);

        return PaginatedConcepts.builder().concepts(conceptsPage.stream().map(ConceptMapper.INSTANCE::toDto)
                        .map(concept -> attachRelatedConcepts(concept, vocabularyCode)).collect(Collectors.toList()))
                .page(pageCoords.getPage()).perpage(pageCoords.getPerpage()).pages(conceptsPage.getTotalPages())
                .hits(conceptsPage.getTotalElements()).count(conceptsPage.getNumberOfElements()).build();
    }


    private ConceptDto attachRelatedConcepts(ConceptDto concept, String vocabularyCode) {
        concept.setRelatedConcepts(conceptRelatedConceptService.getRelatedConcepts(concept.getCode(), vocabularyCode));

        return concept;
    }


    public ConceptDto getConcept(String code, String vocabularyCode) {
        Concept concept = conceptRepository.findById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode)
                        .build()).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code "
                        + vocabularyCode));
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }


    public Concept getConceptByUri(String uri) {
        return conceptRepository.findByUri(uri);
    }


    public ConceptDto createConcept(ConceptCore conceptCore, String vocabularyCode, boolean candidate)
            throws ConceptAlreadyExistsException, VocabularyIsClosedException {
        Vocabulary vocabulary = loadVocabulary(vocabularyCode);
        if (vocabulary.isClosed())
            throw new VocabularyIsClosedException(vocabularyCode);
        String code = conceptFactory.resolveCode(conceptCore, vocabulary);
        Optional<Concept> conceptHolder = conceptRepository.findById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode)
                        .build());
        if (conceptHolder.isPresent()) {
            throw new ConceptAlreadyExistsException(code, vocabularyCode);
        }
        conceptCore.setCode(code);
        Concept concept = conceptFactory.create(conceptCore, vocabulary, null);
        concept.setCandidate(candidate);
        concept.setOrd(getMaxOrdForConceptInVocabulary(vocabulary));
        concept = conceptRepository.save(concept);
        List<ConceptRelatedConcept> conceptRelatedConcepts = conceptFactory.createConceptRelations(concept,
                conceptCore.getRelatedConcepts());
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        indexConceptService.indexConcept(concept, vocabulary);
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }


    private int getMaxOrdForConceptInVocabulary(Vocabulary vocabulary) {
        ExampleMatcher queryConceptMatcher = ExampleMatcher.matching()
                .withMatcher("vocabulary", ExampleMatcher.GenericPropertyMatchers.exact());
        Concept queryConcept = new Concept();
        queryConcept.setVocabulary(vocabulary);
        int count = (int) conceptRepository.count(Example.of(queryConcept, queryConceptMatcher));
        return count + 1;
    }


    public ConceptDto updateConcept(String code, ConceptCore conceptCore, String vocabularyCode) {
        Vocabulary vocabulary = loadVocabulary(vocabularyCode);
        if (!conceptRepository.existsById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode)
                        .build())) {
            throw new EntityNotFoundException(
                    "Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code "
                            + vocabularyCode);
        }
        conceptCore.setCode(code);
        Concept concept = conceptFactory.create(conceptCore, vocabulary, code);
        concept = conceptRepository.save(concept);
        removeConceptAssociations(concept);
        List<ConceptRelatedConcept> conceptRelatedConcepts = conceptFactory.createConceptRelations(concept,
                conceptCore.getRelatedConcepts());
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        indexConceptService.indexConcept(concept, vocabulary);
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }


    public ConceptDto commitConcept(String code, String vocabularyCode) {
        Concept concept = conceptRepository.findById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode)
                        .build()).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code "
                        + vocabularyCode));
        concept.setCandidate(false);
        concept = conceptRepository.save(concept);
        indexConceptService.indexConcept(concept, concept.getVocabulary());
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }


    public Vocabulary loadVocabulary(String vocabularyCode) {
        return vocabularyRepository.findById(vocabularyCode).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));
    }


    public List<Concept> getRelatedConceptsOfConcept(Concept concept, ConceptRelation relation) {
        List<Concept> result = new ArrayList<>();
        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findBySubjectAndRelation(
                concept, relation);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(subjectRelatedConcept);
            result.add(subjectRelatedConcept.getObject());
        }
        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findByObjectAndRelation(
                concept, relation.getInverseOf());
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(objectRelatedConcept);
            result.add(objectRelatedConcept.getSubject());
        }
        return result;
    }


    public List<Concept> saveConcepts(Iterable<Concept> concepts) {
        return conceptRepository.saveAll(concepts);
    }


    public void removeConcept(String code, String vocabularyCode, boolean force) {
        Concept concept = conceptRepository.findById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode)
                        .build()).orElseThrow(() -> new EntityNotFoundException(
                "Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code "
                        + vocabularyCode));
        if (!force && propertyService.existPropertiesWithConcepts(Collections.singletonList(concept))) {
            throw new IllegalArgumentException(String.format(
                    "Cannot remove the concept with code '%s' from the vocabulary '%s' since "
                            + "the operation would remove concepts associated with existing properties. "
                            + "Use force=true parameter to remove the concept "
                            + "and remove properties associated with this concept.", code, vocabularyCode));
        }
        indexConceptService.removeConcept(concept, vocabularyCode);
        propertyService.removePropertiesWithConcepts(Collections.singletonList(concept));
        conceptRepository.delete(concept);
    }


    public void removeConcepts(List<Concept> concepts) {
        concepts.forEach(this::removeConceptAssociations);
        conceptRepository.deleteAll(concepts);
    }


    private void removeConceptAssociations(Concept concept) {
        conceptRelatedConceptRepository.deleteConceptRelations(concept.getCode());
    }


    public List<Concept> getConceptsList(String vocabularyCode) {
        return conceptRepository.findByVocabularyCode(vocabularyCode);
    }


    public ConceptDto mergeConcepts(String code, String vocabularyCode, List<String> with)
            throws VocabularyIsClosedException {

        Vocabulary vocabulary = loadVocabulary(vocabularyCode);
        if (vocabulary.isClosed())
            throw new VocabularyIsClosedException(vocabularyCode);

        Concept concept = conceptRepository.findById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode)
                        .build()).orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to find %s with code %s and vocabulary code %s ", Concept.class.getName(), code, vocabularyCode)));


        List<Concept> conceptsToMerge = with.stream().map(mergeCode -> conceptRepository.findById(
                ConceptId.builder().code(mergeCode).vocabulary(vocabularyCode)
                        .build()).orElseThrow(() -> new EntityNotFoundException(
                String.format("Unable to find %s with code %s and vocabulary code %s ", Concept.class.getName(), code, vocabularyCode)))).collect(Collectors.toList());

        for (Concept mergeConcept : conceptsToMerge) {
            if (mergeConcept.getLabels() != null) {
                mergeConcept.getLabels().forEach((key, value) -> {
                    if (concept.getLabels() != null && !concept.getLabels().isEmpty() && concept.getLabels().containsKey(key))
                        concept.getLabels().put(key, value);
                });
            }


            if (mergeConcept.getDefinitions() != null ) {
                mergeConcept.getDefinitions().forEach((key, value) -> {
                    if (concept.getDefinitions() != null && concept.getDefinitions().containsKey(key))
                        concept.getDefinitions().put(key, value);
                });
            }

            conceptRelatedConceptService.mergeConceptsRelations(concept, mergeConcept, vocabularyCode);

            replaceConceptInItemMedia(concept, mergeConcept);

            propertyService.replaceConceptInProperties(concept, mergeConcept);
        }

        conceptRepository.save(concept);

        with.forEach(mergedCode -> removeConcept(mergedCode, vocabularyCode, true));
        indexConceptService.indexConcept(concept, vocabulary);
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }


    private void replaceConceptInItemMedia(Concept concept, Concept mergeConcept) {

       replaceConcept(itemRepository.findAllByMediaConcept(mergeConcept),concept, mergeConcept);

    }


    private void replaceConcept(List<Item> items, Concept concept, Concept mergeConcept) {
        items.forEach(item -> item.getMedia().forEach((m -> {
            if (m.getConcept().equals(mergeConcept))
                m.setConcept(concept);
        })));
        itemRepository.saveAll(items);
    }

    public void removeVocabularyFromItemMedia(Vocabulary vocabulary) {

        List<Item> items = itemRepository.findAllByMediaConceptVocabulary(vocabulary.getCode());

        for (Item i :items) {

            i.getMedia().forEach((m -> {
                if (m.getConcept().getVocabulary().equals(vocabulary))
                    m.setConcept(null);
            }));
        }
    }

    public boolean existPropertiesFromVocabulary(String vocabularyCode) {
        return itemRepository.existsByMediaConceptVocabularyCode(vocabularyCode);
    }


}
