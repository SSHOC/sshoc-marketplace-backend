package eu.sshopencloud.marketplace.repositories.search.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ForceFacetSortSolrTemplate extends SolrTemplate {

    public ForceFacetSortSolrTemplate(SolrClient solrClient) {
        super(solrClient);
    }


    @Override
    protected SolrQuery constructQuery(SolrDataQuery query, @Nullable Class<?> domainType) {
        SolrQuery solrQuery = super.constructQuery(query, domainType);
        // tag cannot be in the facet field parameters because of the bug in spring-data-solr-4.1.4. So we have to add global parameters for facet sort since the default one is omitted when constructing query
        solrQuery.setFacetSort("count");
        return solrQuery;
    }


}
