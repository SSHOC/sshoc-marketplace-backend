package eu.sshopencloud.marketplace.model.items;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRelatedItemId implements Serializable {

    private Long subject;
    private Long object;
}
