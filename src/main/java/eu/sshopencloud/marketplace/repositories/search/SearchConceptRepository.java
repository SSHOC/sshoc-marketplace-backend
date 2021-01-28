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
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchConceptRepository {

    private final SolrTemplate solrTemplate;

    public FacetPage<IndexConcept> findByQueryAndFilters(String q, boolean advancedSearch,
                                                         List<SearchFilterCriteria> filterCriteria,
                                                         Pageable pageable) {

        SimpleFacetQuery facetQuery = new SimpleFacetQuery(createQueryCriteria(q, advancedSearch))
                .addProjectionOnFields(
                        IndexConcept.CODE_FIELD,
                        IndexConcept.VOCABULARY_CODE_FIELD,
                        IndexConcept.LABEL_FIELD,
                        IndexConcept.NOTATION_FIELD,
                        IndexConcept.DEFINITION_FIELD,
                        IndexConcept.URI_FIELD,
                        IndexConcept.TYPES_FIELD
                )
                .setPageRequest(pageable);

        filterCriteria.stream().forEach(item -> facetQuery.addFilterQuery(new SimpleFilterQuery(item.getFilterCriteria())));
        facetQuery.setFacetOptions(createFacetOptions());

        return solrTemplate.queryForFacetPage(IndexConcept.COLLECTION_NAME, facetQuery, IndexConcept.class, RequestMethod.GET);
    }

    private Criteria createQueryCriteria(String q, boolean advancedSearch) {
        List<QueryPart> queryParts = QueryParser.parseQuery(q, advancedSearch);
        if (queryParts.isEmpty()) {
            return Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).contains("");
        } else {
            Criteria andCriteria = AnyCriteria.any();
            for (QueryPart queryPart : queryParts) {
                Criteria orCriteria = null;
                if (!queryPart.isPhrase()) {
                    Criteria definitionTextCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_FIELD).boost(1f).contains(queryPart.getExpression());
                    Criteria labelCriteria = Criteria.where(IndexConcept.LABEL_TEXT_FIELD).boost(4f).contains(queryPart.getExpression());
                    Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).contains(queryPart.getExpression());
                    orCriteria = definitionTextCriteria.or(labelCriteria).or(notationCriteria);
                }
                Criteria codeCriteria = Criteria.where(IndexConcept.CODE_FIELD).boost(10f).is(queryPart.getExpression());
                Criteria definitionTextEnCriteria = Criteria.where(IndexConcept.DEFINITION_TEXT_EN_FIELD).boost(2f).is(queryPart.getExpression());
                Criteria labelCriteria = Criteria.where(IndexConcept.LABEL_FIELD).boost(4f).is(queryPart.getExpression());
                Criteria labelEngCriteria = Criteria.where(IndexConcept.LABEL_TEXT_EN_FIELD).boost(4f).is(queryPart.getExpression());
                Criteria notationCriteria = Criteria.where(IndexConcept.NOTATION_FIELD).boost(4f).is(queryPart.getExpression());
                if (orCriteria == null) {
                    orCriteria = codeCriteria.or(definitionTextEnCriteria).or(labelCriteria).or(labelEngCriteria).or(notationCriteria);
                } else {
                    orCriteria = orCriteria.or(codeCriteria).or(definitionTextEnCriteria).or(labelCriteria).or(labelEngCriteria).or(notationCriteria);
                }
                andCriteria = andCriteria.and(orCriteria);
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
