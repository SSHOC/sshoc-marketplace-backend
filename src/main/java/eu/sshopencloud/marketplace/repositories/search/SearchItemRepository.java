package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchItemRepository {

    private final SolrTemplate solrTemplate;

    public FacetPage<IndexItem> findByQueryAndFilters(String q, List<SearchFilterCriteria> filterCriteria, List<SearchOrder> order, Pageable pageable) {
        SimpleFacetQuery facetQuery = new SimpleFacetQuery(createQueryCriteria(q))
                .addProjectionOnFields(IndexItem.ID_FIELD, IndexItem.LABEL_FIELD, IndexItem.DESCRIPTION_FIELD, IndexItem.CATEGORY_FIELD)
                .addSort(Sort.by(createQueryOrder(order)))
                .setPageRequest(pageable);

        filterCriteria.stream().forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexItem.COLLECTION_NAME, facetQuery, IndexItem.class, RequestMethod.GET);
    }


    private Criteria createQueryCriteria(String q) {
        List<QueryPart> queryParts = QueryParser.parseQuery(q);
        if (queryParts.isEmpty()) {
            return Criteria.where(IndexItem.LABEL_TEXT_FIELD).boost(4f).contains("");
        } else {
            Criteria andCriteria = null;
            for (QueryPart queryPart : queryParts) {
                Criteria orCriteria = null;
                if (!queryPart.isPhrase()) {
                    Criteria nameTextCriteria = Criteria.where(IndexItem.LABEL_TEXT_FIELD).boost(4f).contains(queryPart.getExpression());
                    Criteria descTextCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_FIELD).boost(3f).contains(queryPart.getExpression());
                    orCriteria = nameTextCriteria.or(descTextCriteria);
                }
                Criteria nameTextEnCriteria = Criteria.where(IndexItem.LABEL_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria descTextEnCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_EN_FIELD).boost(1f).is(queryPart.getExpression());
                if (orCriteria == null) {
                    orCriteria = nameTextEnCriteria.or(descTextEnCriteria);
                } else {
                    orCriteria = orCriteria.or(nameTextEnCriteria.or(descTextEnCriteria));
                }
                if (andCriteria == null) {
                    andCriteria = orCriteria;
                } else {
                    andCriteria = andCriteria.and(orCriteria);
                }
            }
            return andCriteria;
        }
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
        facetOptions.setFacetLimit(-1);
        facetOptions.setFacetMinCount(1);
        facetOptions.setFacetSort(FacetOptions.FacetSort.COUNT);
        Arrays.stream(SearchFacet.values())
                .filter(searchFacet -> searchFacet.getFilter().getIndexType().equals(IndexType.ITEMS))
                .map(SearchFacet::toFacetField)
                .forEach(facetOptions::addFacetOnField);
        return facetOptions;
    }

}
