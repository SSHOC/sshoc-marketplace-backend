package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.*;
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

    @Query("SELECT rel from ConceptRelatedConcept rel where rel.object.vocabulary.code  = :code or rel.subject.vocabulary.code = :code")
    List<ConceptRelatedConcept> getAllConceptRelatedConceptForVocabulary(@Param("code") String code);

    @Modifying
    @Query("delete from ConceptRelatedConcept rel where rel.object.code = :code or rel.subject.code = :code")
    void deleteConceptRelations(@Param("code") String conceptCode);
}
