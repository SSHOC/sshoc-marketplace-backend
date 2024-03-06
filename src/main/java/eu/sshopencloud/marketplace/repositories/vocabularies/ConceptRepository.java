package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, ConceptId> {

    Concept findByUri(String uri);

    Page<Concept> findByVocabularyCode(String vocabularyCode, Pageable pageable);

    List<Concept> findByVocabularyCode(String vocabularyCode);

    Optional<Concept> findByCodeAndVocabularyCode(String code, String vocabularyCode );

    @Query("SELECT COALESCE(MAX(c.ord), 0) FROM Concept c WHERE c.vocabulary.code = :vocabularyCode")
    int findMaxOrdByVocabularyCode(String vocabularyCode);
}
