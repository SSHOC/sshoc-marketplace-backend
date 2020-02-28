package eu.sshopencloud.marketplace.repositories.search;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.services.search.filter.IndexType;
import eu.sshopencloud.marketplace.services.search.filter.SearchFacet;
import eu.sshopencloud.marketplace.services.search.filter.SearchFilterCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchConceptRepository {

    private final SolrTemplate solrTemplate;

    public FacetPage<IndexConcept> findByQueryAndFilters(String q, List<SearchFilterCriteria> filterCriteria, Pageable pageable) {
        SimpleFacetQuery facetQuery = new SimpleFacetQuery(createQueryCriteria(q))
                .addProjectionOnFields(IndexConcept.CODE_FIELD, IndexConcept.VOCABULARY_CODE_FIELD, IndexConcept.LABEL_FIELD, IndexConcept.NOTATION_FIELD, IndexConcept.DEFINITION_FIELD,
                        IndexConcept.URI_FIELD, IndexConcept.TYPES_FIELD)
                .setPageRequest(pageable);

        filterCriteria.stream().forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexConcept.COLLECTION_NAME, facetQuery, IndexConcept.class, RequestMethod.GET);
    }

    private Criteria createQueryCriteria(String q) {
        List<QueryPart> queryParts = QueryParser.parseQuery(q);
        if (queryParts.isEmpty()) {
            return Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).contains("");
        } else {
            Criteria andCriteria = null;
            for (QueryPart queryPart : queryParts) {
                Criteria orCriteria = null;
                if (!queryPart.isPhrase()) {
                    Criteria definitionTextCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_FIELD).boost(1f).contains(queryPart.getExpression());
                    Criteria labelCriteria = Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).contains(queryPart.getExpression());
                    Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).contains(queryPart.getExpression());
                    orCriteria = definitionTextCriteria.or(labelCriteria).or(notationCriteria);
                }
                Criteria definitionTextEnCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria labelFuzzyCriteria = Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).fuzzy(queryPart.getExpression());
                Criteria notationFuzzyCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).fuzzy(queryPart.getExpression());
                if (orCriteria == null) {
                    orCriteria = definitionTextEnCriteria.or(labelFuzzyCriteria).or(notationFuzzyCriteria);
                } else {
                    orCriteria = orCriteria.or(definitionTextEnCriteria.or(labelFuzzyCriteria).or(notationFuzzyCriteria));
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
