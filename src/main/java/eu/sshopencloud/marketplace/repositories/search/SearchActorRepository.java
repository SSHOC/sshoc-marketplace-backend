package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.search.IndexActor;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.search.solr.ForceFacetSortSolrTemplate;
import eu.sshopencloud.marketplace.repositories.search.solr.ForceNestedDocumentsSolrTemplate;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchActorRepository {

    private final ForceFacetSortSolrTemplate solrTemplate;

    public FacetPage<IndexActor> findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                       List<SearchExpressionCriteria> expressionCriteria,
                                                       List<SearchFilterCriteria> filterCriteria,
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
        filterCriteria.forEach(actor -> facetQuery.addFilterQuery(new SimpleFilterQuery(actor.getFilterCriteria())));

        facetQuery.setFacetOptions(createFacetOptions());
        return solrTemplate.queryForFacetPage(IndexActor.COLLECTION_NAME, facetQuery, IndexActor.class, RequestMethod.GET);
    }

    private List<Sort.Order> createQueryOrder(List<SearchOrder> order) {
        List<Sort.Order> result = new ArrayList<Sort.Order>();
        for (SearchOrder o : order) {
            String name = o.getValue().replace('-', '_');
            if (o.isAsc()) {
                result.add(Sort.Order.asc(name));
            } else {
                result.add(Sort.Order.desc(name));
            }
        }
        return result;
    }

    private FacetOptions createFacetOptions() {
        FacetOptions facetOptions = new FacetOptions();
        // tag cannot be in the facet field parameters because of the bug in spring-data-solr-4.1.4. So we add global parameters for facets
        facetOptions.setFacetLimit(-1);
        facetOptions.setFacetMinCount(1);
        facetOptions.setFacetSort(FacetOptions.FacetSort.COUNT);
        Arrays.stream(SearchFacet.values())
                .filter(searchFacet -> searchFacet.getFilter().getIndexType().equals(IndexType.ACTORS))
                .map(SearchFacet::toFacetField)
                .forEach(facetOptions::addFacetOnField);
        return facetOptions;
    }


}
