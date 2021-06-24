package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.model.items.ItemMediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemThumbnailDto {
    private UUID mediaId;
    private String caption;
    private ItemMediaType itemMediaType;
}
