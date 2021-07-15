package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.search.solr.ForceFacetSortSolrTemplate;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchItemRepository {

    private final ForceFacetSortSolrTemplate solrTemplate;


    public FacetPage<IndexItem> findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                      List<SearchExpressionCriteria> expressionCriteria,
                                                      User currentUser,
                                                      List<SearchFilterCriteria> filterCriteria,
                                                      List<SearchOrder> order, Pageable pageable) {

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(queryCriteria.getQueryCriteria())
                .addProjectionOnFields(
                        IndexItem.ID_FIELD,
                        IndexItem.PERSISTENT_ID_FIELD,
                        IndexItem.LABEL_FIELD,
                        IndexItem.DESCRIPTION_FIELD,
                        IndexItem.CATEGORY_FIELD,
                        IndexItem.STATUS_FIELD,
                        IndexItem.OWNER_FIELD,
                        IndexItem.LAST_INFO_UPDATE_FIELD
                )
                .addSort(Sort.by(createQueryOrder(order)))
                .setPageRequest(pageable);

        if (currentUser == null || !currentUser.isModerator()) {
            facetQuery.addFilterQuery(createVisibilityFilter(currentUser));
        }
        expressionCriteria.forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        filterCriteria.forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexItem.COLLECTION_NAME, facetQuery, IndexItem.class, RequestMethod.GET);
    }

    private FilterQuery createVisibilityFilter(User user) {
        Criteria approvedVisibility = new Criteria(IndexItem.STATUS_FIELD).is(ItemStatus.APPROVED.getValue());

        if (user == null || !user.isContributor())
            return new SimpleFilterQuery(approvedVisibility);

        Criteria userVisibility = new Criteria(IndexItem.OWNER_FIELD).is(user.getUsername());
        Criteria visibilityCriteria = approvedVisibility.or(userVisibility);

        return new SimpleFilterQuery(visibilityCriteria);
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
                .filter(searchFacet -> searchFacet.getFilter().getIndexType().equals(IndexType.ITEMS))
                .map(SearchFacet::toFacetField)
                .forEach(facetOptions::addFacetOnField);
        return facetOptions;
    }

    public List<String> autocompleteSearchQuery(String searchQuery) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/marketplace-items/suggest");
        params.set("q", searchQuery);
        params.set("suggest.count", 50);

        try {
            SuggesterResponse response = solrTemplate.getSolrClient()
                    .query(params).getSuggesterResponse();

            List<String> rawSuggestions = response.getSuggestedTerms().get("itemSearch");
            return prepareSuggestions(rawSuggestions, 10);
        }
        catch (SolrServerException | IOException e) {
            throw new RuntimeException("Search engine instance connection error", e);
        }
    }

    private List<String> prepareSuggestions(List<String> suggestions, int limit) {
        Set<String> uniqueSuggestions = new HashSet<>();

        return suggestions.stream()
                .map(String::toLowerCase)
                .filter(suggestion -> {
                    if (uniqueSuggestions.contains(suggestion))
                        return false;
                    else
                        uniqueSuggestions.add(suggestion);

                    return true;
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void rebuildAutocompleteIndex() {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/marketplace-items/suggest/rebuild");

        try {
            solrTemplate.getSolrClient().query(params);
        }
        catch (SolrServerException | IOException e) {
            throw new RuntimeException("Failed to rebuild index for autocomplete");
        }
    }
}
