package eu.sshopencloud.marketplace.dto.items;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class ItemDifferencesCore<S extends ItemCore, T extends ItemDto> {

    S item;
    boolean equal;
    T other;

}
