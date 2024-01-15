package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.dto.search.ItemSearchOrder;
import eu.sshopencloud.marketplace.dto.search.SuggestedObject;
import eu.sshopencloud.marketplace.mappers.items.ItemCategoryConverter;
import eu.sshopencloud.marketplace.model.auth.User;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import eu.sshopencloud.marketplace.repositories.search.solr.ForceFacetSortSolrTemplate;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchExpressionCriteria;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import eu.sshopencloud.marketplace.services.search.query.SearchQueryCriteria;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SimpleParams;
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
    private final SolrClient solrClient;

    public FacetPage<IndexItem> findByQueryAndFilters(SearchQueryCriteria queryCriteria,
                                                      List<SearchExpressionCriteria> expressionCriteria,
                                                      User currentUser, boolean includeSteps,
                                                      List<SearchFilterCriteria> filterCriteria,
                                                      List<ItemSearchOrder> order, Pageable pageable) {

        SolrQuery solrQuery = new SolrQuery();
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

        if (!includeSteps)
            facetQuery.addFilterQuery(createStepFilter());

        if (currentUser == null || !currentUser.isModerator()) {
            facetQuery.addFilterQuery(createVisibilityFilter(currentUser));
        }

        expressionCriteria.forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        filterCriteria.forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));

        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexItem.COLLECTION_NAME, facetQuery, IndexItem.class, RequestMethod.GET);
    }


    public FacetPage<IndexItem> findByQuery(SolrQuery queryCriteria, User currentUser, ItemSearchOrder order, Pageable pageable) {
        List.of(IndexItem.ID_FIELD,
                IndexItem.PERSISTENT_ID_FIELD,
                IndexItem.LABEL_FIELD,
                IndexItem.DESCRIPTION_FIELD,
                IndexItem.CATEGORY_FIELD,
                IndexItem.STATUS_FIELD,
                IndexItem.OWNER_FIELD,
                IndexItem.LAST_INFO_UPDATE_FIELD).forEach(queryCriteria::addField);
        queryCriteria.addSort(createQueryOrder(order));
        queryCriteria.setRows(pageable.getPageSize());
        queryCriteria.setStart((pageable.getPageNumber() - 1) * pageable.getPageSize());

//        SimpleFacetQuery facetQuery = new SimpleFacetQuery(queryCriteria)
//                .addProjectionOnFields(
//                        IndexItem.ID_FIELD,
//                        IndexItem.PERSISTENT_ID_FIELD,
//                        IndexItem.LABEL_FIELD,
//                        IndexItem.DESCRIPTION_FIELD,
//                        IndexItem.CATEGORY_FIELD,
//                        IndexItem.STATUS_FIELD,
//                        IndexItem.OWNER_FIELD,
//                        IndexItem.LAST_INFO_UPDATE_FIELD
//                )
//                .addSort(Sort.by(createQueryOrder(order)))
//                .setPageRequest(pageable);

        if (currentUser == null || !currentUser.isModerator()) {
            queryCriteria.addFilterQuery(createVisibilityFilter(currentUser));
            //facetQuery.addFilterQuery(createVisibilityFilter(currentUser));
        }

        return solrTemplate.queryForFacetPage(IndexItem.COLLECTION_NAME, facetQuery, IndexItem.class, RequestMethod.GET);
    }


    private String createVisibilityFilter(User user) {
        StringBuilder visibilityQuery = new StringBuilder();
        visibilityQuery.append(IndexItem.STATUS_FIELD).append(":").append(ItemStatus.APPROVED.getValue());
        //Criteria approvedVisibility = new Criteria(IndexItem.STATUS_FIELD).is(ItemStatus.APPROVED.getValue());

        if (user == null || !user.isContributor()) {
            return visibilityQuery.toString();
        }
            //return new SimpleFilterQuery(approvedVisibility);
        visibilityQuery.append(StringUtils.SPACE + SimpleParams.OR_OPERATOR + StringUtils.SPACE)
                .append(IndexItem.OWNER_FIELD).append(":").append(user.getUsername());
//        Criteria userVisibility = new Criteria(IndexItem.OWNER_FIELD).is(user.getUsername());
//        Criteria visibilityCriteria = approvedVisibility.or(userVisibility);

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
            //return Sort.Order.asc(name);
        } else {
            return SolrQuery.SortClause.asc(name);
            //return Sort.Order.desc(name);
        }

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

    private FilterQuery createStepFilter() {
        return new SimpleFilterQuery(new Criteria(IndexItem.CATEGORY_FIELD).is(ItemCategory.STEP).not());
    }


}
