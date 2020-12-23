package eu.sshopencloud.marketplace.dto.datasets;

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
public class PaginatedDatasets extends PaginatedResult<DatasetDto> {

    private List<DatasetDto> datasets;

    @Override
    @JsonGetter("datasets")
    public List<DatasetDto> getResults() {
        return datasets;
    }
}
