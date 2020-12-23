package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemBasicDto {

    private Long id;

    private ItemCategory category;

    private String label;

    private String version;

    private String persistentId;
}
