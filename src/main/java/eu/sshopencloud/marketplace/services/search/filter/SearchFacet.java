package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.Getter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.FacetParams;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public enum SearchFacet {

    CATEGORY(SearchFilter.CATEGORY, IndexItem.CATEGORY_FIELD,
            List.of(IndexItem.CATEGORY_FIELD),
            new SearchFacetParameters(6, 0, SearchFacetParameters.FacetSort.INDEX)
    ),

    ACTIVITY(SearchFilter.ACTIVITY, IndexItem.FACETING_DYNAMIC_PROPERTY_ACTIVITY_TAG,
            List.of(IndexItem.FACETING_DYNAMIC_PROPERTY_ACTIVITY_TAG),
            new SearchFacetParameters(-1, 1, SearchFacetParameters.FacetSort.COUNT)
    ),

    SOURCE(SearchFilter.SOURCE, IndexItem.SOURCE_FIELD,
            List.of(IndexItem.SOURCE_FIELD),
            new SearchFacetParameters(-1, 1, SearchFacetParameters.FacetSort.COUNT)
    ),

    KEYWORD(SearchFilter.KEYWORD, IndexItem.FACETING_DYNAMIC_PROPERTY_KEYWORD_TAG,
            List.of(IndexItem.FACETING_DYNAMIC_PROPERTY_KEYWORD_TAG),
            new SearchFacetParameters(-1, 1, SearchFacetParameters.FacetSort.COUNT)
    ),

    LANGUAGE(SearchFilter.LANGUAGE, IndexItem.FACETING_DYNAMIC_PROPERTY_LANGUAGE_TAG,
            List.of(IndexItem.FACETING_DYNAMIC_PROPERTY_LANGUAGE_TAG),
            new SearchFacetParameters(-1, 1, SearchFacetParameters.FacetSort.COUNT)
    ),

    PROPERTY_TYPE(SearchFilter.PROPERTY_TYPE, IndexConcept.TYPES_FIELD,
            Collections.singletonList(IndexConcept.TYPES_FIELD),
            new SearchFacetParameters(-1, 1, SearchFacetParameters.FacetSort.COUNT)
    ),

    CANDIDATE(SearchFilter.CANDIDATE, IndexConcept.CANDIDATE_FIELD,
            Collections.singletonList(IndexConcept.CANDIDATE_FIELD),
            new SearchFacetParameters(-1, 0, SearchFacetParameters.FacetSort.COUNT)
    );


    public static final String TAG_PREFIX = "tag_";

    private final SearchFilter filter;

    private final String tag;

    private final List<String> exclusionTags;

    private final SearchFacetParameters parameters;

    SearchFacet(SearchFilter filter, String field, List<String> exclusionFields, SearchFacetParameters parameters) {
        this.filter = filter;
        this.tag = TAG_PREFIX + field;
        if (exclusionFields == null) {
            this.exclusionTags = Collections.emptyList();
        } else {
            this.exclusionTags = exclusionFields.stream().map(exclusionField -> TAG_PREFIX + exclusionField).collect(Collectors.toList());
        }
        this.parameters = parameters;
    }

    public String getName() {
        return filter.getKey();
    }


    private String toFacetName() {
        if (exclusionTags.isEmpty()) {
            return getName();
        }
        return String.format("{!ex=%s}%s", String.join(",", exclusionTags), getName());
    }

    public String toFacetFieldName() {
        return toFacetName();
    }

    public void updateQuery(SolrQuery solrQuery) {
        solrQuery.addFacetField(toFacetFieldName());
        if (Objects.nonNull(getParameters())) {
            if (Objects.nonNull(getParameters().getLimit())) {
                solrQuery.setParam(
                        "f." + toFacetFieldName() + "." + FacetParams.FACET_LIMIT + "=" + getParameters().getLimit());
            }
            if (Objects.nonNull(getParameters().getMinCount())) {
                solrQuery.setParam("f." + toFacetFieldName() + "." + FacetParams.FACET_MINCOUNT + "=" +
                        getParameters().getMinCount());
            }
            if (Objects.nonNull(getParameters().getSort())) {
                solrQuery.setParam("f." + toFacetFieldName() + "." + FacetParams.FACET_SORT + "=" +
                        getParameters().getSort().toString());
            }
        }
    }

    public String toFilterField() {
        if (exclusionTags.isEmpty()) {
            return getName();
        }
        return String.format("{!tag=%s}%s", tag, getName());
    }

    public static SearchFacet ofFilter(SearchFilter filter) {
        return Arrays.stream(values())
                .filter(facet -> facet.getFilter().equals(filter))
                .findFirst().orElse(null);
    }

}
