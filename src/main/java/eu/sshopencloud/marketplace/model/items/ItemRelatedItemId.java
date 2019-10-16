package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ItemRelatedItemId implements Serializable {

    private Long subjectId;

    private Long objectId;

}
