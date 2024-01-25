package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.ConceptSearchOrder;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchConceptRepository {
    private final List<String> CONCEPT_SEARCH_RESPONSE_FIELDS = List.of(IndexConcept.CODE_FIELD,
            IndexConcept.VOCABULARY_CODE_FIELD,
            IndexConcept.LABEL_FIELD,
            IndexConcept.NOTATION_FIELD,
            IndexConcept.DEFINITION_FIELD,
            IndexConcept.URI_FIELD,
            IndexConcept.TYPES_FIELD,
            IndexConcept.CANDIDATE_FIELD);

    private final SolrClient solrClient;

    public QueryResponse findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                         List<SearchFilterCriteria> filterCriteria,
                                                         Pageable pageable, ConceptSearchOrder order) {
        SolrQuery solrQuery = new SolrQuery(queryCriteria.getQueryCriteria());

        CONCEPT_SEARCH_RESPONSE_FIELDS.forEach(solrQuery::addField);
        solrQuery.addSort(order.toSort());
        solrQuery.setRows(pageable.getPageSize());
        solrQuery.setStart((pageable.getPageNumber()) * pageable.getPageSize());

        filterCriteria.forEach(concept -> solrQuery.addFilterQuery(concept.getFilterCriteria()));

        solrQuery.setFacet(true);
        solrQuery.setFacetLimit(-1);
        solrQuery.setFacetMinCount(1);
        solrQuery.setFacetSort(FacetParams.FACET_SORT_COUNT);

        Arrays.stream(SearchFacet.values())
                .filter(searchFacet -> searchFacet.getFilter().getIndexType().equals(IndexType.CONCEPTS))
                .forEach(sf -> sf.updateQuery(solrQuery));

        try {
            return solrClient.query(IndexConcept.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.GET);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
