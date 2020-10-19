package eu.sshopencloud.marketplace.dto.tools;

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
public class PaginatedTools extends PaginatedResult<ToolDto> {

    private List<ToolDto> tools;

    @Override
    @JsonGetter("tools")
    public List<ToolDto> getResults() {
        return tools;
    }
}
