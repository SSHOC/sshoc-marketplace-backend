package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedSearchCollection extends PaginatedResult<SearchCollection> {

    private String q;

    private List<SearchCollection> collections;

    @Override
    @JsonGetter("actors")
    public List<SearchCollection> getResults() {
        return collections;
    }
}
