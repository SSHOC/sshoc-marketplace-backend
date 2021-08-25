package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.repositories.search.solr.ForceFacetSortSolrTemplate;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchActorRepository {

    private final ForceFacetSortSolrTemplate solrTemplate;

    public FacetPage<IndexActor> findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                       List<SearchExpressionCriteria> expressionCriteria,
                                                       Pageable pageable) {

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(queryCriteria.getQueryCriteria())
                .addProjectionOnFields(
                        IndexActor.ID_FIELD,
                        IndexActor.NAME_FIELD,
                        IndexActor.WEBSITE_FIELD,
                        IndexActor.EMAIL_FIELD,
                        IndexActor.EXTERNAL_IDENTIFIER_FIELD
                )
                .setPageRequest(pageable);

        expressionCriteria.forEach(actor -> facetQuery.addFilterQuery(new SimpleFilterQuery(actor.getFilterCriteria())));

        return solrTemplate.queryForFacetPage(IndexActor.COLLECTION_NAME, facetQuery, IndexActor.class, RequestMethod.GET);
    }

}
