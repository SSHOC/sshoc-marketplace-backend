package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.PageCoords;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PaginatedConcepts;
import eu.sshopencloud.marketplace.mappers.vocabularies.ConceptMapper;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
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
    private final ConceptRelationRepository conceptRelationRepository;
    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;
    private final ConceptRelatedConceptDetachingRepository conceptRelatedConceptDetachingRepository;
    private final ConceptRelatedConceptService conceptRelatedConceptService;


    @Deprecated
    public List<ConceptDto> getObjectTypeConcepts(ItemCategory category) {
        Concept concept = getDefaultObjectTypeConcept(category);
        List<Concept> concepts = new ArrayList<>();
        concepts.add(concept);
        concepts.addAll(getRelatedConceptsOfConcept(concept, conceptRelationRepository.getOne("narrower")));
        concepts.sort(new ConceptComparator());
        return concepts.stream().map(ConceptMapper.INSTANCE::toDto).collect(Collectors.toList());
    }

    @Deprecated
    public Map<ItemCategory, Concept> getAllDefaultObjectTypeConcepts() {
        return Arrays.stream(ItemCategory.indexedCategories())
                .collect(Collectors.toMap(category -> category, this::getDefaultObjectTypeConcept));
    }

    @Deprecated
    public Concept getDefaultObjectTypeConcept(ItemCategory category) {
        return getConcept(category.getValue(), ItemCategory.OBJECT_TYPE_VOCABULARY_CODE);
    }

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

    public Concept getConcept(String code, String vocabularyCode) {
        return conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode));
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

    public void removeConcepts(List<Concept> concepts) {
        concepts.forEach(this::removeConceptAssociations);
        conceptRepository.deleteAll(concepts);
    }

    private void removeConceptAssociations(Concept concept) {
        conceptRelatedConceptRepository.deleteConceptRelations(concept.getCode());
    }
}
