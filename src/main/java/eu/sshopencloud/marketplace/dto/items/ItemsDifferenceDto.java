package eu.sshopencloud.marketplace.dto.items;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ItemsDifferenceDto<S extends ItemDto, T extends ItemDto> {

    S item;
    boolean equal;
    T other;
}
