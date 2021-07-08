package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, ConceptId> {

    Concept findByUri(String uri);

    @Query("select p from Concept p where p.code = :code and p.vocabulary.code = :vocabularyCode")
    Concept findByCodeAndVocabularyCode2(@Param("code") String code, @Param("vocabularyCode") String vocabularyCode);

    Page<Concept> findByVocabularyCode(String vocabularyCode, Pageable pageable);

    List<Concept> findByVocabularyCode(String vocabularyCode, Sort sort);

}
