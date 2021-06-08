package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemMediaDto {
    private MediaDetails info;
    private String caption;
}
