package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexConcept;
import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.Getter;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.SimpleField;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SearchFacet {

    CATEGORY(SearchFilter.CATEGORY, IndexItem.CATEGORY_FIELD,
            Collections.singletonList(IndexItem.CATEGORY_FIELD),
            new SearchFacetParameters(4, 0, FacetOptions.FacetSort.INDEX)
    ),

    PROPERTY_TYPE(SearchFilter.PROPERTY_TYPE, IndexConcept.TYPES_FIELD,
             Collections.singletonList(IndexConcept.TYPES_FIELD),
            new SearchFacetParameters(-1, 1, FacetOptions.FacetSort.COUNT)
    );


    public static final String TAG_PREFIX = "tag_";

    private SearchFilter filter;

    private String tag;

    private List<String> exclusionTags;

    private SearchFacetParameters parameters;

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

    public Field toFacetField() {
        if (parameters != null) {
            FacetOptions.FieldWithFacetParameters facetFieldWithParameters = new FacetOptions.FieldWithFacetParameters(toFacetName());
            facetFieldWithParameters.setLimit(parameters.getLimit());
            facetFieldWithParameters.setMinCount(parameters.getMinCount());
            facetFieldWithParameters.setSort(parameters.getSort());
            return facetFieldWithParameters;
        } else {
            return new SimpleField(toFacetName());
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