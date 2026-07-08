package eu.sshopencloud.marketplace.dto.collections;

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
public class PaginatedCollections extends PaginatedResult<CollectionDto> {

    private List<CollectionDto> collections;

    @Override
    @JsonGetter("collections")
    public List<CollectionDto> getResults() {
        return collections;
    }
}
