package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.*;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRelatedConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.ConceptRepository;
import eu.sshopencloud.marketplace.repositories.vocabularies.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConceptRelatedConceptService {

    private final ConceptRelatedConceptRepository conceptRelatedConceptRepository;

    private final ConceptRepository conceptRepository;

    private final VocabularyRepository vocabularyRepository;

    public List<ConceptRelatedConceptInline> getConceptRelatedConcepts(String conceptCode, String vocabularyCode) {
        List<ConceptRelatedConceptInline> relatedConcepts = new ArrayList<ConceptRelatedConceptInline>();

        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptBySubjectCodeAndSubjectVocabularyCode(conceptCode, vocabularyCode);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            ConceptRelatedConceptInline relatedConcept = new ConceptRelatedConceptInline();
            relatedConcept.setCode(subjectRelatedConcept.getObjectCode());
            relatedConcept.setRelation(subjectRelatedConcept.getRelation());
            Concept concept = conceptRepository.getOne(ConceptId.builder().code(subjectRelatedConcept.getObjectCode()).vocabularyCode(subjectRelatedConcept.getObjectVocabularyCode()).build());
            relatedConcept.setLabel(concept.getLabel());
            relatedConcept.setDescription(concept.getDescription());
            relatedConcept.setUri(concept.getUri());
            relatedConcept.setVocabulary(getRelatedVocabularyForConcept(concept));
            relatedConcepts.add(relatedConcept);
        }

        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptByObjectCodeAndObjectVocabularyCode(conceptCode, vocabularyCode);
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            ConceptRelatedConceptInline relatedConcept = new ConceptRelatedConceptInline();
            relatedConcept.setCode(objectRelatedConcept.getSubjectCode());
            relatedConcept.setRelation(objectRelatedConcept.getRelation().getInverseOf());
            Concept concept = conceptRepository.getOne(ConceptId.builder().code(objectRelatedConcept.getSubjectCode()).vocabularyCode(objectRelatedConcept.getSubjectVocabularyCode()).build());
            relatedConcept.setLabel(concept.getLabel());
            relatedConcept.setDescription(concept.getDescription());
            relatedConcept.setUri(concept.getUri());
            relatedConcept.setVocabulary(getRelatedVocabularyForConcept(concept));
            relatedConcepts.add(relatedConcept);
        }

        return relatedConcepts;
    }

    private VocabularyInline getRelatedVocabularyForConcept(Concept concept) {
        Vocabulary vocabulary = vocabularyRepository.getOne(concept.getVocabularyCode());
        VocabularyInline relatedVocabulary = new VocabularyInline();
        relatedVocabulary.setCode(vocabulary.getCode());
        relatedVocabulary.setLabel(vocabulary.getLabel());
        return relatedVocabulary;
    }

}
