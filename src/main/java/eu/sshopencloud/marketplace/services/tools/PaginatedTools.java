package eu.sshopencloud.marketplace.services.tools;

import eu.sshopencloud.marketplace.model.tools.Tool;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
public class PaginatedTools extends PaginatedResult {

    private List<Tool> tools;

}
