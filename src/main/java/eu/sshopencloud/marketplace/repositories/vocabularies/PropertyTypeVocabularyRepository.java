package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeVocabulary;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeVocabularyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyTypeVocabularyRepository extends JpaRepository<PropertyTypeVocabulary, PropertyTypeVocabularyId> {

    List<PropertyTypeVocabulary> findByPropertyTypeCode(String propertyTypeCode);
    List<PropertyTypeVocabulary> findByVocabularyCode(String vocabularyCode);

    void deleteByVocabularyCode(String vocabularyCode);
    void deleteByPropertyTypeCode(String propertyTypeCode);
}
