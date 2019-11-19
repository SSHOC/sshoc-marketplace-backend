package eu.sshopencloud.marketplace.services.search.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.solr.core.query.Criteria;

@Getter
@AllArgsConstructor
public abstract class SearchFilterCriteria {

    private SearchFilter filter;

    public abstract Criteria getFilterCriteria();

    protected String getFilterFieldSpecifier() {
        SearchFacet searchFacet = SearchFacet.ofFilter(getFilter());
        return (searchFacet != null) ? searchFacet.toFilterField() : getFilter().getKey();
    }

}
