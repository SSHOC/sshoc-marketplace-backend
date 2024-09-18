package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.ItemSearchOrder;
import eu.sshopencloud.marketplace.dto.search.SuggestedObject;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SimpleParams;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchItemRepository {

    public static final List<String> RESPONSE_FIELDS = List.of(IndexItem.ID_FIELD, IndexItem.PERSISTENT_ID_FIELD,
            IndexItem.LABEL_FIELD, IndexItem.DESCRIPTION_FIELD, IndexItem.CATEGORY_FIELD, IndexItem.STATUS_FIELD,
            IndexItem.OWNER_FIELD, IndexItem.LAST_INFO_UPDATE_FIELD, IndexItem.ACCESSIBLE_AT, IndexItem.THUMBNAIL_ID);
    private final SolrClient solrClient;

    public QueryResponse findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                      List<SearchExpressionCriteria> expressionCriteria,
                                                      User currentUser, boolean includeSteps,
                                                      List<SearchFilterCriteria> filterCriteria,
                                                      List<ItemSearchOrder> order, Pageable pageable) {

        SolrQuery solrQuery = new SolrQuery(queryCriteria.getQueryCriteria());
        RESPONSE_FIELDS.forEach(solrQuery::addField);
        createQueryOrder(order).forEach(solrQuery::addSort);
        solrQuery.setRows(pageable.getPageSize());
        solrQuery.setStart((pageable.getPageNumber()) * pageable.getPageSize());

        if (!includeSteps) {
            solrQuery.addFilterQuery("-" + IndexItem.CATEGORY_FIELD + ":" + ItemCategory.STEP);
        }

        if (currentUser == null || !currentUser.isModerator()) {
            solrQuery.addFilterQuery(createVisibilityFilter(currentUser));
        }

        expressionCriteria.forEach(item -> solrQuery.addFilterQuery(item.getFilterCriteria()));
        filterCriteria.forEach(item -> solrQuery.addFilterQuery(item.getFilterCriteria()));

        solrQuery.setFacet(true);
        solrQuery.setFacetLimit(-1);
        solrQuery.setFacetMinCount(1);
        solrQuery.setFacetSort(FacetParams.FACET_SORT_COUNT);

        Arrays.stream(SearchFacet.values())
                .filter(searchFacet -> searchFacet.getFilter().getIndexType().equals(IndexType.ITEMS))
                .forEach(sf -> sf.updateQuery(solrQuery));

        try {
            return solrClient.query(IndexItem.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.GET);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public QueryResponse findByQuery(SolrQuery solrQuery, User currentUser, ItemSearchOrder order, Pageable pageable) {
        RESPONSE_FIELDS.forEach(solrQuery::addField);
        solrQuery.addSort(createQueryOrder(order));
        solrQuery.setRows(pageable.getPageSize());
        solrQuery.setStart((pageable.getPageNumber()) * pageable.getPageSize());

        if (currentUser == null || !currentUser.isModerator()) {
            solrQuery.addFilterQuery(createVisibilityFilter(currentUser));
        }
        try {
            return solrClient.query(IndexItem.COLLECTION_NAME, solrQuery, SolrRequest.METHOD.GET);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String createVisibilityFilter(User user) {
        StringBuilder visibilityQuery = new StringBuilder();
        visibilityQuery.append(IndexItem.STATUS_FIELD).append(":").append(ItemStatus.APPROVED.getValue());

        if (user == null || !user.isContributor()) {
            return visibilityQuery.toString();
        }

        visibilityQuery.append(StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE)
                .append(IndexItem.OWNER_FIELD).append(":").append(user.getUsername());

        return visibilityQuery.toString();
    }

    private List<SolrQuery.SortClause> createQueryOrder(List<ItemSearchOrder> order) {
        List<SolrQuery.SortClause> result = new ArrayList<>();
        for (ItemSearchOrder o : order) {
            result.add(createQueryOrder(o));
        }
        return result;
    }

    private SolrQuery.SortClause createQueryOrder(ItemSearchOrder order) {
        String name = order.getValue().replace('-', '_');
        if (order.isAsc()) {
            return SolrQuery.SortClause.asc(name);
        } else {
            return SolrQuery.SortClause.desc(name);
        }

    }

    public List<SuggestedObject> autocompleteSearchQuery(String searchQuery, ItemCategory context) {
        String categoryContext = null;

        if (context != null) {
            categoryContext = ItemCategoryConverter.convertCategoryForAutocompleteContext(context);
        }

        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/marketplace-items/suggest");
        params.set("dictionary", "itemSearch");
        params.set("q", searchQuery);
        if(categoryContext==null || categoryContext.toLowerCase(Locale.ROOT).equals("step"))
            categoryContext = "-step";
        else
            categoryContext = categoryContext + " AND -step";
        params.set("suggest.cfq", categoryContext);
        params.set("suggest.count", 50);

        try {
            SuggesterResponse response = solrClient.query(params).getSuggesterResponse();

            List<Suggestion> rawPayload = response.getSuggestions().get("itemSearch");
            return prepareSuggestions(rawPayload);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException("Search engine instance connection error", e);
        }
    }


    private List<SuggestedObject> prepareSuggestions(List<Suggestion> rawPayload) {
        return rawPayload.stream().map(s -> new SuggestedObject(s.getTerm(), s.getPayload()))
                .distinct().limit(10)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void rebuildAutocompleteIndex() {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/marketplace-items/suggest/rebuild");

        try {
            solrClient.query(params);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException("Failed to rebuild index for autocomplete");
        }
    }
}
