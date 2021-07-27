package eu.sshopencloud.marketplace.repositories.sources;

import eu.sshopencloud.marketplace.model.items.Item;
import eu.sshopencloud.marketplace.model.sources.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    Source findByDomain(String domain);

    @Query(value =
            "  SELECT DISTINCT (s.id), s.domain, s.label, s.last_harvested_date, s.url, s.url_template "+
                    " FROM sources s  " +
                    " INNER JOIN items i" +
                    " ON i.source_id = s.id"+
                    " INNER JOIN versioned_items v "+
                    " ON i.persistent_id = v.id" +
                    " WHERE v.merged_with_id = :persistentId OR i.persistent_id = :persistentId", nativeQuery = true
    )
    List<Source> findSources(@Param("persistentId" ) String persistentId);

}
