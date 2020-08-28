package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConcept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelatedConceptId;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptRelatedConceptRepository extends JpaRepository<ConceptRelatedConcept, ConceptRelatedConceptId> {

    List<ConceptRelatedConcept> findBySubjectCodeAndSubjectVocabularyCode(String subjectCode, String subjectVocabularyCode);
    List<ConceptRelatedConcept> findByObjectCodeAndObjectVocabularyCode(String objectCode, String objectVocabularyCode);
    List<ConceptRelatedConcept> findBySubjectAndRelation(Concept subject, ConceptRelation relation);
    List<ConceptRelatedConcept> findByObjectAndRelation(Concept object, ConceptRelation relation);

    @Modifying
    @Query("delete from ConceptRelatedConcept rel where rel.object.code = :code or rel.subject.code = :code")
    void deleteConceptRelations(@Param("code") String conceptCode);
}
