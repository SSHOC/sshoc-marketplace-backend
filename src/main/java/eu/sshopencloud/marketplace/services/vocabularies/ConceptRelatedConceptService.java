package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptRelatedConceptService {

    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;

    private final ConceptRelatedConceptDetachingRepository conceptRelatedConceptDetachingRepository;

    private final VocabularyRepository vocabularyRepository;

    public List<ConceptRelatedConceptInline> getConceptRelatedConcepts(String conceptCode, String vocabularyCode) {
        List<ConceptRelatedConceptInline> relatedConcepts = new ArrayList<ConceptRelatedConceptInline>();

        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findBySubjectCodeAndSubjectVocabularyCode(conceptCode, vocabularyCode);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(subjectRelatedConcept);
            VocabularyInline objectVocabulary = getRelatedVocabularyForConcept(subjectRelatedConcept.getObject().getVocabulary().getCode());
            ConceptRelatedConceptInline relatedConcept = ConceptConverter.convertRelatedConceptFromSubject(subjectRelatedConcept, objectVocabulary);
            relatedConcepts.add(relatedConcept);
        }

        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findByObjectCodeAndObjectVocabularyCode(conceptCode, vocabularyCode);
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detach(objectRelatedConcept);
            VocabularyInline subjectVocabulary = getRelatedVocabularyForConcept(objectRelatedConcept.getSubject().getVocabulary().getCode());
            ConceptRelatedConceptInline relatedConcept = ConceptConverter.convertRelatedConceptFromObject(objectRelatedConcept, subjectVocabulary);
            relatedConcepts.add(relatedConcept);
        }

        return relatedConcepts;
    }

    private VocabularyInline getRelatedVocabularyForConcept(String vocabularyCode) {
        Vocabulary vocabulary = vocabularyRepository.getOne(vocabularyCode);
        VocabularyInline relatedVocabulary = new VocabularyInline();
        relatedVocabulary.setCode(vocabulary.getCode());
        relatedVocabulary.setLabel(vocabulary.getLabel());
        return relatedVocabulary;
    }


    public List<ConceptRelatedConcept> validateReflexivityAndSave(List<ConceptRelatedConcept> newConceptRelatedConcepts) {
        List<ConceptRelatedConcept> conceptRelatedConcepts = new ArrayList<ConceptRelatedConcept>();
        for (ConceptRelatedConcept newConceptRelatedConcept: newConceptRelatedConcepts) {
            ConceptRelatedConcept conceptRelatedConcept = validateReflexivityAndSave(newConceptRelatedConcept);
            if (conceptRelatedConcept != null) {
                conceptRelatedConcepts.add(conceptRelatedConcept);
            }
        }
        return conceptRelatedConcepts;
    }

    private ConceptRelatedConcept validateReflexivityAndSave(ConceptRelatedConcept newConceptRelatedConcept) {
        ConceptRelatedConceptId dirId = new ConceptRelatedConceptId();
        dirId.setSubject(new ConceptId(newConceptRelatedConcept.getSubject().getCode(), newConceptRelatedConcept.getSubject().getVocabulary().getCode()));
        dirId.setObject(new ConceptId(newConceptRelatedConcept.getObject().getCode(), newConceptRelatedConcept.getObject().getVocabulary().getCode()));
        Optional<ConceptRelatedConcept> dirConceptRelatedConcept = conceptRelatedConceptRepository.findById(dirId);
        if (dirConceptRelatedConcept.isPresent()) {
            return null;
        }
        ConceptRelatedConceptId revId = new ConceptRelatedConceptId();
        revId.setSubject(new ConceptId(newConceptRelatedConcept.getObject().getCode(), newConceptRelatedConcept.getObject().getVocabulary().getCode()));
        revId.setObject(new ConceptId(newConceptRelatedConcept.getSubject().getCode(), newConceptRelatedConcept.getSubject().getVocabulary().getCode()));
        Optional<ConceptRelatedConcept> revConceptRelatedConcept = conceptRelatedConceptRepository.findById(revId);
        if (revConceptRelatedConcept.isPresent()) {
            return null;
        }

        ConceptRelatedConcept conceptRelatedConcept = conceptRelatedConceptRepository.save(newConceptRelatedConcept);
        return conceptRelatedConcept;
    }

}
