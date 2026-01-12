package eu.sshopencloud.marketplace.dto.workflows;

import com.twelvemonkeys.util.LinkedSet;
import eu.sshopencloud.marketplace.model.items.ItemFlag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for workflow creation process
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class WorkflowCreationCore extends WorkflowCore {

    private Set<ItemFlag> flags = new LinkedSet<>();
}
