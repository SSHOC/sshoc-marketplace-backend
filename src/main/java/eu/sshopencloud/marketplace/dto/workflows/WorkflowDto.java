package eu.sshopencloud.marketplace.dto.workflows;


import eu.sshopencloud.marketplace.dto.items.ItemDto;
import eu.sshopencloud.marketplace.model.items.ItemFlag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WorkflowDto extends ItemDto {

    private List<StepDto> composedOf = new ArrayList<>();

    private Set<ItemFlag> flags = new HashSet<>();

}
