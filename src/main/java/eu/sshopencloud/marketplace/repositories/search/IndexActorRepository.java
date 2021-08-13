package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.search.IndexActor;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexActorRepository extends SolrCrudRepository<IndexActor, String> {

    @Query(value = "root_b:true", fields = {"*", "[child parentFilter=root_b:true]"})
    Iterable<IndexActor> findAllIndexActorWithVariants();
}
