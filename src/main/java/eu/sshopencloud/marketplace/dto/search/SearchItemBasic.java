package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SearchItemBasic {

    private Long id;

    private String persistentId;

    private ItemCategory category;

    private String label;

    private String version;

    private String lastInfoUpdate;

}
