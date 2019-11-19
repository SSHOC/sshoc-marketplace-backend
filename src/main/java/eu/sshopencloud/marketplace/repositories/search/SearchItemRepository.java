package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.search.IndexItem;
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
        // TODO extend indexItem with more values and handle then in projections

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(createQueryCriteria(q))
                .addProjectionOnFields(IndexItem.ID_FIELD, IndexItem.NAME_FIELD, IndexItem.DESCRIPTION_FIELD, IndexItem.CATEGORY_FIELD)
                .addSort(Sort.by(createQueryOrder(order)))
                .setPageRequest(pageable);

        filterCriteria.stream().forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexItem.COLLECTION_NAME, facetQuery, IndexItem.class, RequestMethod.GET);
    }


    private Criteria createQueryCriteria(String q) {
        List<QueryPart> queryParts = parseQuery(q);
        if (queryParts.isEmpty()) {
            return Criteria.where(IndexItem.NAME_TEXT_FIELD).boost(4f).contains("");
        } else {
            Criteria andCriteria = null;
            for (QueryPart queryPart : queryParts) {
                Criteria orCriteria = null;
                if (!queryPart.isPhrase()) {
                    Criteria nameTextCriteria = Criteria.where(IndexItem.NAME_TEXT_FIELD).boost(4f).contains(queryPart.getExpression());
                    Criteria descTextCriteria = Criteria.where(IndexItem.DESCRIPTION_TEXT_FIELD).boost(3f).contains(queryPart.getExpression());
                    orCriteria = nameTextCriteria.or(descTextCriteria);
                }
                Criteria nameTextEnCriteria = Criteria.where(IndexItem.NAME_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
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

    private List<QueryPart> parseQuery(String q) {
        log.debug("Original query: {}", q);
        List<QueryPart> result = new ArrayList<QueryPart>();
        String[] words = q.split(" ", -1);
        boolean phrase = false;
        String expression = "";
        for (String word : words) {
            if (!word.isEmpty()) {
                if (phrase) {
                    if (word.endsWith("\"")) {
                        expression += " " + word;
                        result.add(new QueryPart(expression, true));
                        expression = "";
                        phrase = false;
                    } else {
                        expression += " " + word;
                    }
                } else {
                    if (word.startsWith("\"")) {
                        expression += word;
                        phrase = true;
                        if (word.endsWith("\"")) {
                            result.add(new QueryPart(expression, true));
                            expression = "";
                            phrase = false;
                        }
                    } else {
                        expression = word;
                        result.add(new QueryPart(expression, false));
                        expression = "";
                    }
                }
            }
        }
        log.debug("Combined query: {}", result);
        return result;
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
        // TODO set limit, min count and sort independently for each facet
        facetOptions.setFacetLimit(-1);
        facetOptions.setFacetMinCount(0);
        facetOptions.setFacetSort(FacetOptions.FacetSort.INDEX);
        Arrays.stream(SearchFacet.values()).map(SearchFacet::toFacetField).forEach(facetOptions::addFacetOnField);
        return facetOptions;
    }

}
