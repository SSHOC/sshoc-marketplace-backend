package eu.sshopencloud.marketplace.model.items;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemRelatedItemInline {

    private Long id;

    private String url;

    private String label;

    private String description;

    private ItemRelation relation;

}
