package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptDetachingRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConceptRelatedConceptService {

    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;

    private final ConceptRelatedConceptDetachingRepository conceptRelatedConceptDetachingRepository;

    private final VocabularyRepository vocabularyRepository;

    public List<ConceptRelatedConceptInline> getConceptRelatedConcepts(String conceptCode, String vocabularyCode) {
        List<ConceptRelatedConceptInline> relatedConcepts = new ArrayList<ConceptRelatedConceptInline>();

        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptBySubjectCodeAndSubjectVocabularyCode(conceptCode, vocabularyCode);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detachConceptRelatedConcept(subjectRelatedConcept);
            ConceptRelatedConceptInline relatedConcept = new ConceptRelatedConceptInline();
            relatedConcept.setCode(subjectRelatedConcept.getObject().getCode());
            relatedConcept.setRelation(subjectRelatedConcept.getRelation());
            Concept concept = subjectRelatedConcept.getObject();
            relatedConcept.setLabel(concept.getLabel());
            relatedConcept.setDefinition(concept.getDefinition());
            relatedConcept.setUri(concept.getUri());
            relatedConcept.setVocabulary(getRelatedVocabularyForConcept(subjectRelatedConcept.getObject().getVocabulary().getCode()));
            relatedConcepts.add(relatedConcept);
        }

        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptByObjectCodeAndObjectVocabularyCode(conceptCode, vocabularyCode);
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            conceptRelatedConceptDetachingRepository.detachConceptRelatedConcept(objectRelatedConcept);
            ConceptRelatedConceptInline relatedConcept = new ConceptRelatedConceptInline();
            relatedConcept.setCode(objectRelatedConcept.getSubject().getCode());
            relatedConcept.setRelation(objectRelatedConcept.getRelation().getInverseOf());
            Concept concept = objectRelatedConcept.getSubject();
            relatedConcept.setLabel(concept.getLabel());
            relatedConcept.setDefinition(concept.getDefinition());
            relatedConcept.setUri(concept.getUri());
            relatedConcept.setVocabulary(getRelatedVocabularyForConcept(objectRelatedConcept.getSubject().getVocabulary().getCode()));
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

}
