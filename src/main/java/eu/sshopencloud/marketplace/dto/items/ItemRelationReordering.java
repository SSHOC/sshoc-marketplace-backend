package eu.sshopencloud.marketplace.dto.items;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRelationReordering {
    @NotNull
    private List<ItemRelationReorder> shifts;
}
