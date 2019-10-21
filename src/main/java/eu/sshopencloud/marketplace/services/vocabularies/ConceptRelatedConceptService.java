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

    public List<ConceptRelatedConceptInline> getConceptRelatedConcepts(Long conceptId) {
        List<ConceptRelatedConceptInline> relatedConcepts = new ArrayList<ConceptRelatedConceptInline>();

        List<ConceptRelatedConcept> subjectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptBySubjectId(conceptId);
        for (ConceptRelatedConcept subjectRelatedConcept : subjectRelatedConcepts) {
            ConceptRelatedConceptInline relatedConcept = new ConceptRelatedConceptInline();
            relatedConcept.setId(subjectRelatedConcept.getObjectId());
            relatedConcept.setRelation(subjectRelatedConcept.getRelation());
            Concept concept = conceptRepository.getOne(subjectRelatedConcept.getObjectId());
            relatedConcept.setLabel(concept.getLabel());
            relatedConcept.setDescription(concept.getDescription());
            relatedConcept.setUri(concept.getUri());
            relatedConcept.setVocabulary(getRelatedVocabularyForConcept(concept));
            relatedConcepts.add(relatedConcept);
        }

        List<ConceptRelatedConcept> objectRelatedConcepts = conceptRelatedConceptRepository.findConceptRelatedConceptByObjectId(conceptId);
        for (ConceptRelatedConcept objectRelatedConcept : objectRelatedConcepts) {
            ConceptRelatedConceptInline relatedConcept = new ConceptRelatedConceptInline();
            relatedConcept.setId(objectRelatedConcept.getSubjectId());
            relatedConcept.setRelation(objectRelatedConcept.getRelation().getInverseOf());
            Concept concept = conceptRepository.getOne(objectRelatedConcept.getSubjectId());
            relatedConcept.setLabel(concept.getLabel());
            relatedConcept.setDescription(concept.getDescription());
            relatedConcept.setUri(concept.getUri());
            relatedConcept.setVocabulary(getRelatedVocabularyForConcept(concept));
            relatedConcepts.add(relatedConcept);
        }

        return relatedConcepts;
    }

    private VocabularyInline getRelatedVocabularyForConcept(Concept concept) {
        Vocabulary vocabulary = vocabularyRepository.findVocabularyByConcepts(concept);
        VocabularyInline relatedVocabulary = new VocabularyInline();
        relatedVocabulary.setCode(vocabulary.getCode());
        relatedVocabulary.setLabel(vocabulary.getLabel());
        return relatedVocabulary;
    }

}
