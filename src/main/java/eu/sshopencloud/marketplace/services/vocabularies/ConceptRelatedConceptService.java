package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.RelatedConceptDto;
import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.mappers.vocabularies.ConceptConverter;
import eu.sshopencloud.marketplace.mappers.vocabularies.VocabularyBasicMapper;
import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConceptRelatedConceptService {

    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;

    private final ConceptRelatedConceptDetachingRepository conceptRelatedConceptDetachingRepository;

    private final VocabularyRepository vocabularyRepository;


    public List<RelatedConceptDto> getRelatedConcepts(String conceptCode, String vocabularyCode) {
        List<RelatedConceptDto> relatedConcepts = new ArrayList<>();

        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findBySubjectCodeAndSubjectVocabularyCode(
                conceptCode, vocabularyCode);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(subjectRelatedConcept);
            VocabularyBasicDto objectVocabulary = getRelatedVocabularyForConcept(
                    subjectRelatedConcept.getObject().getVocabulary().getCode());
            RelatedConceptDto relatedConcept = ConceptConverter.convertRelatedConceptFromSubject(subjectRelatedConcept,
                    objectVocabulary);
            relatedConcepts.add(relatedConcept);
        }

        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findByObjectCodeAndObjectVocabularyCode(
                conceptCode, vocabularyCode);
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(objectRelatedConcept);
            VocabularyBasicDto subjectVocabulary = getRelatedVocabularyForConcept(
                    objectRelatedConcept.getSubject().getVocabulary().getCode());
            RelatedConceptDto relatedConcept = ConceptConverter.convertRelatedConceptFromObject(objectRelatedConcept,
                    subjectVocabulary);
            relatedConcepts.add(relatedConcept);
        }

        return relatedConcepts;
    }


    public List<ConceptRelatedConcept> getConceptRelatedConcept(String conceptCode, String vocabularyCode) {

        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findBySubjectCodeAndSubjectVocabularyCode(
                conceptCode, vocabularyCode);

        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(subjectRelatedConcept);
        }

        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findByObjectCodeAndObjectVocabularyCode(
                conceptCode, vocabularyCode);
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(objectRelatedConcept);
        }

        subjectRelatedConcepts.addAll(objectRelatedConcepts);

        validateVocabulary(subjectRelatedConcepts);

        return subjectRelatedConcepts;
    }


    private VocabularyBasicDto getRelatedVocabularyForConcept(String vocabularyCode) {
        Vocabulary vocabulary = vocabularyRepository.getOne(vocabularyCode);
        return VocabularyBasicMapper.INSTANCE.toDto(vocabulary);
    }


    public List<ConceptRelatedConcept> validateReflexivityAndSave(
            List<ConceptRelatedConcept> newConceptRelatedConcepts) {
        List<ConceptRelatedConcept> conceptRelatedConcepts = new ArrayList<>();
        for (ConceptRelatedConcept newConceptRelatedConcept : newConceptRelatedConcepts) {
            ConceptRelatedConcept conceptRelatedConcept = validateReflexivityAndSave(newConceptRelatedConcept);
            if (conceptRelatedConcept != null) {
                conceptRelatedConcepts.add(conceptRelatedConcept);
            }
        }
        return conceptRelatedConcepts;
    }


    private ConceptRelatedConcept validateReflexivityAndSave(ConceptRelatedConcept newConceptRelatedConcept) {
        ConceptRelatedConceptId dirId = new ConceptRelatedConceptId();
        dirId.setSubject(new ConceptId(newConceptRelatedConcept.getSubject().getCode(),
                newConceptRelatedConcept.getSubject().getVocabulary().getCode()));
        dirId.setObject(new ConceptId(newConceptRelatedConcept.getObject().getCode(),
                newConceptRelatedConcept.getObject().getVocabulary().getCode()));
        Optional<ConceptRelatedConcept> dirConceptRelatedConcept = conceptRelatedConceptRepository.findById(dirId);
        if (dirConceptRelatedConcept.isPresent()) {
            return null;
        }
        ConceptRelatedConceptId revId = new ConceptRelatedConceptId();
        revId.setSubject(new ConceptId(newConceptRelatedConcept.getObject().getCode(),
                newConceptRelatedConcept.getObject().getVocabulary().getCode()));
        revId.setObject(new ConceptId(newConceptRelatedConcept.getSubject().getCode(),
                newConceptRelatedConcept.getSubject().getVocabulary().getCode()));
        Optional<ConceptRelatedConcept> revConceptRelatedConcept = conceptRelatedConceptRepository.findById(revId);
        if (revConceptRelatedConcept.isPresent()) {
            return null;
        }

        return conceptRelatedConceptRepository.save(newConceptRelatedConcept);
    }


    public void validateVocabulary(List<ConceptRelatedConcept> objectRelatedConcepts) {
        objectRelatedConcepts.forEach(o -> {
            if (!o.getObject().getVocabulary().getCode().equals(o.getSubject().getVocabulary().getCode()))
                throw new IllegalArgumentException(
                        String.format("Can't have concept relation with different vocabulary code %s and %s",
                                o.getObject().getVocabulary(), o.getSubject().getVocabulary()));
        });

    }


    public void mergeConceptsRelations(Concept concept, Concept conceptToReplace, String vocabularyCode) {

        List<ConceptRelatedConcept> concepts = getConceptRelatedConcept(concept.getCode(), vocabularyCode);
        List<ConceptRelatedConcept> conceptsToMerge = getConceptRelatedConcept(conceptToReplace.getCode(), vocabularyCode);
        List<ConceptRelatedConcept> newRelations = new ArrayList<>();

        conceptsToMerge.forEach(mergeConcept -> {
            if (!concepts.contains(mergeConcept) && !mergeConcept.getSubject().equals(concept)
                    && !mergeConcept.getObject().equals(concept)) {
                ConceptRelatedConcept newConcept = new ConceptRelatedConcept();
                if (mergeConcept.getObject().equals(conceptToReplace)) {
                    newConcept.setObject(concept);
                    newConcept.setSubject(mergeConcept.getSubject());
                } else {
                    newConcept.setObject(mergeConcept.getObject());
                    newConcept.setSubject(concept);
                }

                newConcept.setRelation(mergeConcept.getRelation());
                newRelations.add(newConcept);
            }

        });

        List<ConceptRelatedConcept> conceptsToRemove = concepts.stream().filter(c -> c.getSubject().equals(conceptToReplace) || c.getObject().equals(conceptToReplace)).collect(
                Collectors.toList());

        conceptRelatedConceptRepository.deleteAll(conceptsToRemove);
        conceptRelatedConceptRepository.deleteAll(conceptsToMerge);

        validateReflexivityAndSave(newRelations);

    }

}
