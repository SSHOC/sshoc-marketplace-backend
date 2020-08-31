package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyTypeRepository extends JpaRepository<PropertyType, String> {

    @Query("select max(ord) from PropertyType")
    Integer findMaxPropertyTypeOrd();

    @Modifying
    @Query("update PropertyType pt set pt.ord = pt.ord + :shift where pt.ord > :ord")
    void shiftSucceedingPropertyTypesOrder(@Param("ord") int ord, @Param("shift") int shift);
}
