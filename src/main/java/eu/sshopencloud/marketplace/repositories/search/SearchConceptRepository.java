package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.ConceptSearchOrder;
import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchConceptRepository {

    private final SolrTemplate solrTemplate;

    public FacetPage<IndexConcept> findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                         List<SearchFilterCriteria> filterCriteria,
                                                         Pageable pageable, ConceptSearchOrder order) {

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(queryCriteria.getQueryCriteria())
                .addProjectionOnFields(
                        IndexConcept.CODE_FIELD,
                        IndexConcept.VOCABULARY_CODE_FIELD,
                        IndexConcept.LABEL_FIELD,
                        IndexConcept.NOTATION_FIELD,
                        IndexConcept.DEFINITION_FIELD,
                        IndexConcept.URI_FIELD,
                        IndexConcept.TYPES_FIELD,
                        IndexConcept.CANDIDATE_FIELD
                )
                .setPageRequest(pageable).addSort(order.toSort());

        filterCriteria.forEach(concept -> facetQuery.addFilterQuery(new SimpleFilterQuery(concept.getFilterCriteria())));
        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexConcept.COLLECTION_NAME, facetQuery, IndexConcept.class, RequestMethod.GET);
    }

    private FacetOptions createFacetOptions() {
        FacetOptions facetOptions = new FacetOptions();
        // tag cannot be in the facet field parameters because of the bug in spring-data-solr-4.1.4. So we add global parameters for facets
        facetOptions.setFacetLimit(-1);
        facetOptions.setFacetMinCount(1);
        facetOptions.setFacetSort(FacetOptions.FacetSort.COUNT);
        Arrays.stream(SearchFacet.values())
                .filter(searchFacet -> searchFacet.getFilter().getIndexType().equals(IndexType.CONCEPTS))
                .map(SearchFacet::toFacetField)
                .forEach(facetOptions::addFacetOnField);
        return facetOptions;
    }

}
