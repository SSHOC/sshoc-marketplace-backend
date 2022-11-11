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
public class PaginatedSearchItemsBasic extends PaginatedResult<SearchItemBasic> {

    private List<SearchItemBasic> items;

    @Override
    @JsonGetter("items")
    public List<SearchItemBasic> getResults() {
        return items;
    }

}
