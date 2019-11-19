package eu.sshopencloud.marketplace.services.search.filter;

import lombok.Getter;
import org.springframework.data.solr.core.query.Criteria;

import java.util.List;

@Getter
public class SearchFilterValuesSelection extends SearchFilterCriteria {

    private List<String> values;

    public SearchFilterValuesSelection(SearchFilter filter, List<String> values) {
        super(filter);
        this.values = values;
    }

    @Override
    public Criteria getFilterCriteria() {
        Criteria filterField = new Criteria(getFilterFieldSpecifier());
        return filterField.in(getValues());
    }

}
