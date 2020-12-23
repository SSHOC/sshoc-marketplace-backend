package eu.sshopencloud.marketplace.dto.workflows;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedWorkflows extends PaginatedResult<WorkflowDto> {

    private List<WorkflowDto> workflows;

    @Override
    @JsonGetter("workflows")
    public List<WorkflowDto> getResults() {
        return workflows;
    }
}
