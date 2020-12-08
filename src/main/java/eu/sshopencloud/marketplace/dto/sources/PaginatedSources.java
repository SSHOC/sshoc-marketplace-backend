package eu.sshopencloud.marketplace.dto.sources;

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
public class PaginatedSources extends PaginatedResult<SourceDto> {

    private List<SourceDto> sources;

    @Override
    @JsonGetter("sources")
    public List<SourceDto> getResults() {
        return sources;
    }
}
