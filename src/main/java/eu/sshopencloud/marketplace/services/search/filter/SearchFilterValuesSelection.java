package eu.sshopencloud.marketplace.services.search.filter;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SearchFilterValuesSelection extends SearchFilterCriteria {

    private final List<String> values;

    public SearchFilterValuesSelection(SearchFilter filter, List<String> values) {
        super(filter);
        this.values = values;
    }

    @Override
    public String getFilterCriteria() {
        return getFilterFieldSpecifier() + ":(" + StringUtils.join(getValues().stream().map(v -> "\"" + v +"\"").collect(
                Collectors.toList()), StringUtils.SPACE) + ")";
    }
}
