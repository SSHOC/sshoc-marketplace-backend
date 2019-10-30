package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.search.SearchItem;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchItemRepository extends SolrCrudRepository<SearchItem, Long> {

}
