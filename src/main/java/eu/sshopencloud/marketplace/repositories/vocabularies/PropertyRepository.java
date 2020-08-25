package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByItemIdOrderByOrd(Long itemId);

    @Modifying
    @Query("delete from Property p where p.concept.code in :conceptCodes")
    void deletePropertiesWithConcepts(@Param("conceptCodes") List<String> conceptCodes);
}
