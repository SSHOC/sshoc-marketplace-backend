package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.search.IndexCollection;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SimpleParams;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchCollectionRepository {

    private static final List<String> SEARCH_COLLECTION_RESPONSE_FIELDS = List.of(
            IndexCollection.ID_FIELD,
            IndexCollection.TITLE_FIELD,
            IndexCollection.DESCRIPTION_FIELD,
            IndexCollection.ITEMS_COUNT_FIELD,
            IndexCollection.CREATED_AT_FIELD,
            IndexCollection.UPDATED_AT_FIELD,
            IndexCollection.OWNER_NAME_FIELD);

    private final SolrClient solrClient;

    public QueryResponse findByQuery(SearchQueryCriteria queryCriteria, User currentUser, Pageable pageable) {

        SolrQuery solrQuery = new SolrQuery(queryCriteria.getQueryCriteria());
        SEARCH_COLLECTION_RESPONSE_FIELDS.forEach(solrQuery::addField);
        solrQuery.setRows(pageable.getPageSize());
        solrQuery.setStart((pageable.getPageNumber()) * pageable.getPageSize());
        solrQuery.addFilterQuery(createVisibilityFilter(currentUser));

        try {
            return solrClient.query(IndexCollection.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.POST);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createVisibilityFilter(User user) {
        StringBuilder visibilityQuery = new StringBuilder();

        if (user == null) {
            visibilityQuery.append("visible:true");
        }else{
            visibilityQuery.append("visible:true")
                    .append(StringUtils.SPACE)
                    .append(SimpleParams.OR_OPERATOR)
                    .append(StringUtils.SPACE)
                    .append("(visible:false")
                    .append(StringUtils.SPACE)
                    .append(SimpleParams.AND_OPERATOR)
                    .append(StringUtils.SPACE)
                    .append("owner_id:")
                    .append(StringUtils.SPACE)
                    .append(user.getId())
                    .append(")");

        }
        return visibilityQuery.toString();
    }
}
