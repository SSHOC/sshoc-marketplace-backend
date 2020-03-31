package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import lombok.RequiredArgsConstructor;
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


    public List<Concept> getObjectTypeConcepts(ItemCategory category) {
        Concept concept = getDefaultObjectTypeConcept(category);
        List<Concept> relatedConcepts = getRelatedConceptsOfConcept(concept, conceptRelationRepository.getOne("narrower"));
        List<Concept> result = new ArrayList<Concept>();
        result.add(concept);
        result.addAll(relatedConcepts);
        result.sort(new ConceptComparator());
        return result;
    }

    public Map<ItemCategory, Concept> getAllDefaultObjectTypeConcepts() {
        return Arrays.stream(ItemCategory.values())
                .collect(Collectors.toMap(category -> category, this::getDefaultObjectTypeConcept));
    }

    public Concept getDefaultObjectTypeConcept(ItemCategory category) {
        return getConcept(category.getValue(), ItemCategory.OBJECT_TYPE_VOCABULARY_CODE);
    }

    public List<Concept> getConcepts(String vocabularyCode) {
        return conceptRepository.findConceptByVocabularyCode(vocabularyCode, Sort.by(Sort.Order.asc("ord")));
    }

    public Concept getConcept(String code, String vocabularyCode) {
        return conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(code).vocabulary(vocabularyCode).build())
                .orElseThrow(() -> new EntityNotFoundException("Unable to find " + Concept.class.getName() + " with code " + code + " and vocabulary code " + vocabularyCode));
    }

    public List<Concept> getRelatedConceptsOfConcept(Concept concept, ConceptRelation relation) {
        List<Concept> result = new ArrayList<Concept>();
        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptBySubjectAndRelation(concept, relation);
        for (ConceptRelatedConcept subjectRelatedConcept: subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detachConceptRelatedConcept(subjectRelatedConcept);
            result.add(subjectRelatedConcept.getObject());
        }
        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptByObjectAndRelation(concept, relation.getInverseOf());
        for (ConceptRelatedConcept objectRelatedConcept: objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detachConceptRelatedConcept(objectRelatedConcept);
            result.add(objectRelatedConcept.getSubject());
        }
        return result;
    }

}
