package eu.sshopencloud.marketplace.dto.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSourceDto {
    private String code;
    private String label;
}
