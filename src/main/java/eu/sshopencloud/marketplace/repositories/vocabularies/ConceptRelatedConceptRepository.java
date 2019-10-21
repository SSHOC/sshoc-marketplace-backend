package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConceptId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptRelatedConceptRepository extends JpaRepository<ConceptRelatedConcept, ConceptRelatedConceptId> {

    List<ConceptRelatedConcept> findConceptRelatedConceptBySubjectCodeAndSubjectVocabularyCode(String subjectCode, String subjectVocabularyCode);

    List<ConceptRelatedConcept> findConceptRelatedConceptByObjectCodeAndObjectVocabularyCode(String objectCode, String objectVocabularyCode);

}
