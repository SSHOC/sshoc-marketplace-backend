package eu.sshopencloud.marketplace.services.search.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.solr.common.params.FacetParams;

@Getter
@AllArgsConstructor
public class SearchFacetParameters {

    public enum FacetSort {
        INDEX(FacetParams.FACET_SORT_INDEX), COUNT(FacetParams.FACET_SORT_COUNT);

        @Getter
        private final String sort;

        FacetSort(String sort) {
            this.sort = sort;
        }

        @Override
        public String toString() {
            return getSort();
        }
    }

    private Integer limit;

    private Integer minCount;

    private FacetSort sort;

}
