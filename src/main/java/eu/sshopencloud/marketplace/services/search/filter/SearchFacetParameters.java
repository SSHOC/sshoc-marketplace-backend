package eu.sshopencloud.marketplace.services.search.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.solr.core.query.FacetOptions;

@Getter
@AllArgsConstructor
public class SearchFacetParameters {

    private Integer limit;

    private Integer minCount;

    private FacetOptions.FacetSort sort;

}
