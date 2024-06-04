package eu.sshopencloud.marketplace.services.search.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class SearchFilterCriteria {

    private SearchFilter filter;

    public abstract String getFilterCriteria();

    protected String getFilterFieldSpecifier() {
        SearchFacet searchFacet = SearchFacet.ofFilter(getFilter());
        return (searchFacet != null) ? searchFacet.toFilterField() : getFilter().getKey();
    }

}
