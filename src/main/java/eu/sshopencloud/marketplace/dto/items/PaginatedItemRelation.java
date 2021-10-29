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
public class PaginatedItemRelation extends PaginatedResult<ItemRelationDto> {

    private List<ItemRelationDto> itemRelations;

    @Override
    @JsonGetter("itemRelations")
    public List<ItemRelationDto> getResults() {
        return itemRelations;
    }
}
