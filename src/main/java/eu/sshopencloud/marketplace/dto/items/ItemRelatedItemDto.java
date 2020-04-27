package eu.sshopencloud.marketplace.dto.items;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ItemRelatedItemDto {

    private ItemBasicDto subject;

    private ItemBasicDto object;

    private ItemRelationDto relation;

}
