package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedSearchActor extends PaginatedResult<SearchActor> {

    private String q;

    private List<SearchActor> actors;

    private Map<String, Map<String, CheckedCount>> facets;

    @Override
    @JsonGetter("actors")
    public List<SearchActor> getResults() {
        return actors;
    }
}
