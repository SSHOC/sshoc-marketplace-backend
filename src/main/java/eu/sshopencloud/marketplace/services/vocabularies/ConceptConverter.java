package eu.sshopencloud.marketplace.services.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConceptInline;
import eu.sshopencloud.marketplace.model.vocabularies.VocabularyInline;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConceptConverter {

    public Concept convertWithoutVocabulary(Concept concept) {
        Concept result = new Concept();
        result.setCode(concept.getCode());
        result.setLabel(concept.getLabel());
        result.setNotation(concept.getNotation());
        result.setOrd(concept.getOrd());
        result.setDefinition(concept.getDefinition());
        result.setUri(concept.getUri());
        result.setRelatedConcepts(concept.getRelatedConcepts());
        return result;
    }

    public ConceptRelatedConceptInline convertRelatedConceptFromSubject(ConceptRelatedConcept subjectRelatedConcept, VocabularyInline vocabulary) {
        ConceptRelatedConceptInline result = new ConceptRelatedConceptInline();
        result.setCode(subjectRelatedConcept.getObject().getCode());
        result.setRelation(subjectRelatedConcept.getRelation());
        completeRelatedConcept(result, subjectRelatedConcept.getObject(), vocabulary);
        return result;
    }

    public ConceptRelatedConceptInline convertRelatedConceptFromObject(ConceptRelatedConcept objectRelatedConcept, VocabularyInline vocabulary) {
        ConceptRelatedConceptInline result = new ConceptRelatedConceptInline();
        result.setCode(objectRelatedConcept.getSubject().getCode());
        result.setRelation(objectRelatedConcept.getRelation().getInverseOf());
        completeRelatedConcept(result, objectRelatedConcept.getSubject(), vocabulary);
        return result;
    }

    private ConceptRelatedConceptInline completeRelatedConcept(ConceptRelatedConceptInline relatedConcept, Concept concept, VocabularyInline vocabulary) {
        relatedConcept.setLabel(concept.getLabel());
        relatedConcept.setNotation(concept.getNotation());
        relatedConcept.setDefinition(concept.getDefinition());
        relatedConcept.setUri(concept.getUri());
        relatedConcept.setVocabulary(vocabulary);
        return relatedConcept;
    }

}
