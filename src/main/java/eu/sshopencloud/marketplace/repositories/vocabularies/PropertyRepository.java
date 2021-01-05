package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Property;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Modifying
    @Query("delete from Property p where p.concept.code in :conceptCodes")
    void deletePropertiesWithConcepts(@Param("conceptCodes") List<String> conceptCodes);

    @Modifying
    @Query("delete from Property p where p.type = :propertyType")
    void deletePropertiesOfType(@Param("propertyType") PropertyType propertyType);

    boolean existsByConceptVocabularyCode(String vocabularyCode);

    boolean existsByType(PropertyType type);
}
