package eu.sshopencloud.marketplace.dto.items;

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
public class PaginatedItemsBasic<T extends ItemBasicDto> extends PaginatedResult<T> {

    private List<T> items;

    @Override
    @JsonGetter("items")
    public List<T> getResults() {
        return items;
    }

}
