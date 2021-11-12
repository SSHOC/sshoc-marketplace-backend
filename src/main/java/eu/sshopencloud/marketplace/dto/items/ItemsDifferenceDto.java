package eu.sshopencloud.marketplace.dto.items;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ItemsDifferenceDto {

    ItemDto item;
    boolean equal;
    ItemDto otherItem;
}
