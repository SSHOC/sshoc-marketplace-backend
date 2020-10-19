package eu.sshopencloud.marketplace.dto.vocabularies;

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
public class PaginatedConcepts extends PaginatedResult<ConceptDto> {

    private List<ConceptDto> concepts;

    @Override
    @JsonGetter("concepts")
    public List<ConceptDto> getResults() {
        return concepts;
    }
}
