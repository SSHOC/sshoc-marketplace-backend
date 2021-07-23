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
import eu.sshopencloud.marketplace.validators.vocabularies.ConceptFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ConceptFactory conceptFactory;
    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;
    private final ConceptRelatedConceptDetachingRepository conceptRelatedConceptDetachingRepository;
    private final ConceptRelatedConceptService conceptRelatedConceptService;


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

    public List<Concept> getConcepts(String vocabularyCode) {
        return conceptRepository.findByVocabularyCode(vocabularyCode, Sort.by(Sort.Order.asc("ord")));
    }

    public ConceptDto getConcept(String code, String vocabularyCode) {
        Concept concept = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode));
        ConceptDto conceptDto = ConceptMapper.INSTANCE.toDto(concept);
        return attachRelatedConcepts(conceptDto, vocabularyCode);
    }

    public ConceptDto createConcept(ConceptCore conceptCore, String vocabularyCode, boolean candidate) {
        Optional<Concept> conceptHolder = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(conceptCore.getCode()).vocabulary(vocabularyCode).build());
        if (conceptHolder.isPresent()) {

        }
        Concept concept = conceptFactory.create(conceptCore, vocabularyCode, candidate, null);
        // TODO
        return null;
    }

    public ConceptDto updateConcept(ConceptCore conceptCore, String vocabularyCode, boolean candidate) {

        // TODO
        return null;
    }

    public ConceptDto commitConcept(String code, String vocabularyCode) {

        // TODO
        return null;
    }

    public List<Concept> getRelatedConceptsOfConcept(Concept concept, ConceptRelation relation) {
        List<Concept> result = new ArrayList<>();
        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findBySubjectAndRelation(concept, relation);
        for (ConceptRelatedConcept subjectRelatedConcept: subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(subjectRelatedConcept);
            result.add(subjectRelatedConcept.getObject());
        }
        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findByObjectAndRelation(concept, relation.getInverseOf());
        for (ConceptRelatedConcept objectRelatedConcept: objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(objectRelatedConcept);
            result.add(objectRelatedConcept.getSubject());
        }
        return result;
    }

    public List<Concept> saveConcepts(Iterable<Concept> concepts) {
        return conceptRepository.saveAll(concepts);
    }


    public void removeConcept(String code, String vocabularyCode, boolean force) {
    }

    public void removeConcepts(List<Concept> concepts) {
        concepts.forEach(this::removeConceptAssociations);
        conceptRepository.deleteAll(concepts);
    }

    private void removeConceptAssociations(Concept concept) {
        conceptRelatedConceptRepository.deleteConceptRelations(concept.getCode());
    }

}
