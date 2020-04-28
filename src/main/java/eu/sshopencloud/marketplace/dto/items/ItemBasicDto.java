package eu.sshopencloud.marketplace.dto.items;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemBasicDto {

    private Long id;

    private String label;

    private String version;

}
