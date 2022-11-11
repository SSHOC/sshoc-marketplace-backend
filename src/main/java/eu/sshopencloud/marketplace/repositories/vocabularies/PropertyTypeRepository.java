package eu.sshopencloud.marketplace.repositories.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyTypeRepository extends JpaRepository<PropertyType, String> {

    Page<PropertyType> findAllByHiddenIsFalse(Pageable pageable);
    List<PropertyType> findAllByHiddenIsFalse(Sort order);

    Page<PropertyType> findAllByLabelContainingIgnoreCase(String query, Pageable pageable);
    Page<PropertyType> findAllByLabelContainingIgnoreCaseAndHiddenIsFalse(String query, Pageable pageable);
}
