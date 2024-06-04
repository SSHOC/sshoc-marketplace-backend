package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.ActorSearchOrder;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchActorRepository {
    private final List<String> SEARCH_ACTOR_RESPONSE_FIELDS = List.of(IndexActor.ID_FIELD,
            IndexActor.NAME_FIELD,
            IndexActor.WEBSITE_FIELD,
            IndexActor.EMAIL_FIELD,
            IndexActor.EXTERNAL_IDENTIFIER_FIELD);

    private final SolrClient solrClient;

    public QueryResponse findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                       List<SearchExpressionCriteria> expressionCriteria,
                                                       Pageable pageable, ActorSearchOrder order) {

        SolrQuery solrQuery = new SolrQuery(queryCriteria.getQueryCriteria());
        SEARCH_ACTOR_RESPONSE_FIELDS.forEach(solrQuery::addField);
        solrQuery.setRows(pageable.getPageSize());
        solrQuery.setStart((pageable.getPageNumber()) * pageable.getPageSize());
        solrQuery.addSort(order.toSort());

        expressionCriteria.forEach(actor -> solrQuery.addFilterQuery(actor.getFilterCriteria()));

        try {
            return solrClient.query(IndexActor.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.GET);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
