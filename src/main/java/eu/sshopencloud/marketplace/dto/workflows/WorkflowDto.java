package eu.sshopencloud.marketplace.dto.workflows;


import eu.sshopencloud.marketplace.dto.items.ItemDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WorkflowDto extends ItemDto {

    private List<StepDto> composedOf = new ArrayList<>();

}
