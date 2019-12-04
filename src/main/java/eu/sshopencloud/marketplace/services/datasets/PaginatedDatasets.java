package eu.sshopencloud.marketplace.services.datasets;

import eu.sshopencloud.marketplace.model.datasets.Dataset;
import eu.sshopencloud.marketplace.services.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
public class PaginatedDatasets extends PaginatedResult {

    private List<Dataset> datasets;

}
