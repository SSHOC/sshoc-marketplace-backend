package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SearchItemRepository {

    private final SolrTemplate solrTemplate;

    public FacetPage<IndexItem> findByQueryAndCategories(String q, List<String> categories, List<String> order, Pageable pageable) {

        // TODO apply category filter and read facet
        // TODO extend indexItem with more values and handle then in projections

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(createQueryCriteria(q))
                .addProjectionOnFields("id", "name", "description", "category")
                .addSort(Sort.by(order.stream().toArray(String[]::new)));

        return solrTemplate.queryForFacetPage(IndexItem.COLLECTION_NAME, facetQuery, IndexItem.class, RequestMethod.GET);
    }

    private Criteria createQueryCriteria(String q) {
        // TODO english stemming
        Criteria nameCriteria = Criteria.where("name").boost(2f).contains(q);
        Criteria descCriteria = Criteria.where("description").boost(1f).contains(q);
        return nameCriteria.or(descCriteria);
    }

}
