package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.dto.PaginatedResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class PaginatedSearchConcepts extends PaginatedResult {

    private String q;

    private List<SearchConcept> concepts;

    private Map<String, CountedPropertyType> types;

}
