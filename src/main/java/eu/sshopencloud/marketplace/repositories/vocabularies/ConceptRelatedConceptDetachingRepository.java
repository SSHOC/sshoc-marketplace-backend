package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class ConceptRelatedConceptDetachingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void detachConceptRelatedConcept(ConceptRelatedConcept conceptRelatedConcept) {
        entityManager.detach(conceptRelatedConcept);
    }

}
