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
public class PaginatedPropertyTypes extends PaginatedResult<PropertyTypeDto> {

    private List<PropertyTypeDto> propertyTypes;

    @Override
    @JsonGetter("propertyTypes")
    public List<PropertyTypeDto> getResults() {
        return propertyTypes;
    }
}
