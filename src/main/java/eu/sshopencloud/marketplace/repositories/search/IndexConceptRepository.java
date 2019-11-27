package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexConceptRepository extends SolrCrudRepository<IndexConcept, String> {

}
