package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Concept;
import eu.sshopencloud.marketplace.model.vocabularies.ConceptId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, ConceptId> {

    Concept findByUri(String uri);

    Concept findByCodeAndVocabularyCode(String code, String vocabularyCode);

    Page<Concept> findByVocabularyCode(String vocabularyCode, Pageable pageable);

    List<Concept> findByVocabularyCode(String vocabularyCode, Sort sort);

}
