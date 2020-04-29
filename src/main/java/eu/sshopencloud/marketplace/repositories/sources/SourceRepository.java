package eu.sshopencloud.marketplace.repositories.sources;

import eu.sshopencloud.marketplace.model.sources.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    Source findByDomain(String domain);

}
