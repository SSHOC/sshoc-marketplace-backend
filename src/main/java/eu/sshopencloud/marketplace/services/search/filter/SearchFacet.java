package eu.sshopencloud.marketplace.services.search.filter;

import eu.sshopencloud.marketplace.model.search.IndexItem;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SearchFacet {

    CATEGORY(SearchFilter.CATEGORY, IndexItem.CATEGORY_FIELD,
            Collections.singletonList(IndexItem.CATEGORY_FIELD)
    );

    public static final String TAG_PREFIX = "tag_";

    private SearchFilter filter;

    private String tag;

    private List<String> exclusionTags;

    SearchFacet(SearchFilter filter, String field, List<String> exclusionFields) {
        this.filter = filter;
        this.tag = TAG_PREFIX + field;
        if (exclusionFields == null) {
            this.exclusionTags = Collections.emptyList();
        } else {
            this.exclusionTags = exclusionFields.stream().map(exclusionField -> TAG_PREFIX + exclusionField).collect(Collectors.toList());
        }
    }

    public String getName() {
        return filter.getKey();
    }

    public String toFacetField() {
        if (exclusionTags.isEmpty()) {
            return getName();
        }
        return String.format("{!ex=%s}%s", String.join(",", exclusionTags), getName());
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
