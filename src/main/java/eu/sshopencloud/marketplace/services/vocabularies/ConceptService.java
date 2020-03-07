package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelationRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.services.DataViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
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

    public Concept validate(ItemCategory category, String prefix, ConceptId concept, PropertyType propertyType, List<VocabularyInline> allowedVocabularies)
            throws DataViolationException, ConceptDisallowedException, DisallowedObjectTypeException {
        if (concept.getUri() == null) {
            if (concept.getCode() == null) {
                throw new DataViolationException(prefix + "code", concept.getCode());
            }
            if (concept.getVocabulary() == null) {
                throw new DataViolationException(prefix + "vocabulary", "null");
            }
            if (concept.getVocabulary().getCode() == null) {
                throw new DataViolationException(prefix + "vocabulary.code", concept.getVocabulary().getCode());
            }
        }
        Concept result = getConceptByCodeOrUri(prefix, concept);
        if (!allowedVocabularies.stream().anyMatch(v -> Objects.equals(v.getCode(), result.getVocabulary().getCode()))) {
            throw new ConceptDisallowedException(propertyType, result.getVocabulary().getCode());
        }
        if (category != null) {
            checkObjectType(category, prefix + "code", propertyType, result);
        }
        return result;
    }

    private Concept getConceptByCodeOrUri(String prefix, ConceptId concept) throws DataViolationException {
        if (concept.getUri() == null) {
            Optional<Concept> result = conceptRepository.findById(eu.sshopencloud.marketplace.model.vocabularies.ConceptId.builder().code(concept.getCode()).vocabulary(concept.getVocabulary().getCode()).build());
            if (!result.isPresent()) {
                throw new DataViolationException(prefix + "code", concept.getCode());
            }
            return result.get();
        } else {
            Concept result = conceptRepository.findConceptByUri(concept.getUri());
            if (result == null) {
                throw new DataViolationException(prefix + "uri", concept.getUri());
            }
            return result;
        }
    }

    private void checkObjectType(ItemCategory category, String prefix, PropertyType propertyType, Concept concept)
            throws DisallowedObjectTypeException {
        if (propertyType.getCode().equals(ItemCategory.OBJECT_TYPE_PROPERTY_TYPE_CODE)) {
            if (!concept.getCode().equals(category.getValue())) {
                List<Concept> relatedConcepts = getRelatedConceptsOfConcept(concept, conceptRelationRepository.getOne("broader"));
                for (Concept relatedConcept: relatedConcepts) {
                    if (relatedConcept.getCode().equals(category.getValue())) {
                        return;
                    }
                }
                throw new DisallowedObjectTypeException(category, prefix, concept.getCode());
            }
        }
    }

    private List<Concept> getRelatedConceptsOfConcept(Concept concept, ConceptRelation relation) {
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
