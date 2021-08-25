package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptCore;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedConcepts;
import eu.sshopencloud.marketplace.mappers.vocabularies.ConceptMapper;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import eu.sshopencloud.marketplace.services.search.IndexService;
import eu.sshopencloud.marketplace.services.vocabularies.exception.ConceptAlreadyExistsException;
import eu.sshopencloud.marketplace.validators.vocabularies.ConceptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Index;
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
    private final IndexService indexService;


    public PaginatedConcepts getConcepts(String vocabularyCode, PageCoords pageCoords) {
        PageRequest pageRequest = PageRequest.of(
                pageCoords.getPage() - 1, pageCoords.getPerpage(), Sort.by(Sort.Order.asc("ord"))
        );

        Page<Concept> conceptsPage = conceptRepository.findByVocabularyCode(vocabularyCode, pageRequest);

        return PaginatedConcepts.builder()
                .concepts(
                        conceptsPage.stream()
                                .map(ConceptMapper.INSTANCE::toDto)
                                .map(concept -> attachRelatedConcepts(concept, vocabularyCode))
                                .collect(Collectors.toList())
                )
                .page(pageCoords.getPage())
                .perpage(pageCoords.getPerpage())
                .pages(conceptsPage.getTotalPages())
                .hits(conceptsPage.getTotalElements())
                .count(conceptsPage.getNumberOfElements())
                .build();
    }

    private ConceptDto attachRelatedConcepts(ConceptDto concept, String vocabularyCode) {
        concept.setRelatedConcepts(
                conceptRelatedConceptService.getRelatedConcepts(concept.getCode(), vocabularyCode)
        );

        return concept;
    }

    public ConceptDto getConcept(String code, String vocabularyCode) {
        Concept concept = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode));
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }

    public Concept getConceptByUri(String uri) {
        return conceptRepository.findByUri(uri);
    }

    public ConceptDto createConcept(ConceptCore conceptCore, String vocabularyCode, boolean candidate) throws ConceptAlreadyExistsException {
        Vocabulary vocabulary = loadVocabulary(vocabularyCode);
        String code = conceptFactory.resolveCode(conceptCore, vocabulary);
        Optional<Concept> conceptHolder = conceptRepository.findById(
                eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build());
        if (conceptHolder.isPresent()) {
            throw new ConceptAlreadyExistsException(code, vocabularyCode);
        }
        conceptCore.setCode(code);
        Concept concept = conceptFactory.create(conceptCore, vocabulary, null);
        concept.setCandidate(candidate);
        concept.setOrd(getMaxOrdForConceptInVocabulary(vocabulary));
        concept = conceptRepository.save(concept);
                List<ConceptRelatedConcept> conceptRelatedConcepts = conceptFactory.createConceptRelations(concept, conceptCore.getRelatedConcepts());
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        indexService.indexConcept(concept, vocabulary);
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
        if (!conceptRepository.existsById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())) {
            throw new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode);
        }
        conceptCore.setCode(code);
        Concept concept = conceptFactory.create(conceptCore, vocabulary, code);
        concept = conceptRepository.save(concept);
        removeConceptAssociations(concept);
        List<ConceptRelatedConcept> conceptRelatedConcepts = conceptFactory.createConceptRelations(concept, conceptCore.getRelatedConcepts());
        conceptRelatedConceptService.validateReflexivityAndSave(conceptRelatedConcepts);

        indexService.indexConcept(concept, vocabulary);
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }

    public ConceptDto commitConcept(String code, String vocabularyCode) {
        Concept concept = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())
            .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode));
        concept.setCandidate(false);
        concept = conceptRepository.save(concept);
        indexService.indexConcept(concept, concept.getVocabulary());
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }

    public Vocabulary loadVocabulary(String vocabularyCode) {
        return vocabularyRepository.findById(vocabularyCode)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Vocabulary.class.getName() + " with code " + vocabularyCode));
    }


    public List<Concept> getRelatedConceptsOfConcept(Concept concept, ConceptRelation relation) {
        List<Concept> result = new ArrayList<>();
        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findBySubjectAndRelation(concept, relation);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(subjectRelatedConcept);
            result.add(subjectRelatedConcept.getObject());
        }
        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findByObjectAndRelation(concept, relation.getInverseOf());
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
        Concept concept = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode));
        if (!force && propertyService.existPropertiesWithConcepts(Collections.singletonList(concept))) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot remove the concept with code '%s' from the vocabulary '%s' since " +
                                    "the operation would remove concepts associated with existing properties. " +
                                    "Use force=true parameter to remove the concept " +
                                    "and remove properties associated with this concept.",
                            code, vocabularyCode
                    )
            );
        }
        indexService.removeConcept(concept, vocabularyCode);
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

}
