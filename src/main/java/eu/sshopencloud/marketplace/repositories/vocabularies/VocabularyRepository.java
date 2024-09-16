package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Vocabulary;
import eu.sshopencloud.marketplace.repositories.vocabularies.projection.VocabularyBasicView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, String> {

    Page<VocabularyBasicView> findAllBasicBy(Pageable pageable);

    @Query(value = "SELECT v, ptv.propertyType FROM Vocabulary v, PropertyTypeVocabulary ptv where v = ptv.vocabulary and ptv.propertyType.code in :collect")
    List<Object[]> findAllByPropertyTypeCode(List<String> collect);
}
