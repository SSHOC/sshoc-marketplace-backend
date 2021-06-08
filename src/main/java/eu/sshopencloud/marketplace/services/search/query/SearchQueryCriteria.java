package eu.sshopencloud.marketplace.services.search.query;

import org.springframework.data.solr.core.query.Criteria;


public interface SearchQueryCriteria {

    Criteria getQueryCriteria();

}
