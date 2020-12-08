package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelatedItemDto {

    private Long id;
    private String persistentId;

    private ItemCategory category;

    private String label;

    private String description;

    private ItemRelationDto relation;

}
