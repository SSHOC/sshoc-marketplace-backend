package eu.sshopencloud.marketplace.mappers.vocabularies;

import eu.sshopencloud.marketplace.dto.vocabularies.VocabularyBasicDto;
import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.dto.vocabularies.RelatedConceptDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConceptConverter {

    public RelatedConceptDto convertRelatedConceptFromSubject(ConceptRelatedConcept subjectRelatedConcept, VocabularyBasicDto vocabulary) {
        RelatedConceptDto relatedConcept = new RelatedConceptDto();
        relatedConcept.setCode(subjectRelatedConcept.getObject().getCode());
        relatedConcept.setRelation(ConceptRelationMapper.INSTANCE.toDto(subjectRelatedConcept.getRelation()));
        completeRelatedConcept(relatedConcept, subjectRelatedConcept.getObject(), vocabulary);
        return relatedConcept;
    }

    public RelatedConceptDto convertRelatedConceptFromObject(ConceptRelatedConcept objectRelatedConcept, VocabularyBasicDto vocabulary) {
        RelatedConceptDto relatedConcept = new RelatedConceptDto();
        relatedConcept.setCode(objectRelatedConcept.getSubject().getCode());
        relatedConcept.setRelation(ConceptRelationMapper.INSTANCE.toDto(objectRelatedConcept.getRelation().getInverseOf()));
        completeRelatedConcept(relatedConcept, objectRelatedConcept.getSubject(), vocabulary);
        return relatedConcept;
    }

    private RelatedConceptDto completeRelatedConcept(RelatedConceptDto relatedConcept, Concept concept, VocabularyBasicDto vocabulary) {
        relatedConcept.setLabel(concept.getLabel());
        relatedConcept.setNotation(concept.getNotation());
        relatedConcept.setDefinition(concept.getDefinition());
        relatedConcept.setUri(concept.getUri());
        relatedConcept.setVocabulary(vocabulary);
        relatedConcept.setCandidate(concept.isCandidate());
        return relatedConcept;
    }

}
