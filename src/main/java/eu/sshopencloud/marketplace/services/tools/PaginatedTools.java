package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.dto.tools.ToolDto;
import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedTools extends PaginatedResult {

    private List<ToolDto> tools;

}
