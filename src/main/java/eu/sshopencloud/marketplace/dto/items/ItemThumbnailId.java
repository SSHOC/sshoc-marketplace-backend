package eu.sshopencloud.marketplace.dto.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemThumbnailId {
    private UUID mediaId;
}
