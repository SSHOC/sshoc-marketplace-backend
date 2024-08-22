package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ConceptRelatedConceptDetachingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void detach(ConceptRelatedConcept conceptRelatedConcept) {
        entityManager.detach(conceptRelatedConcept);
    }

}
