package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@NoArgsConstructor
public class PropertyTypesReordering {

    @NotNull
    private List<PropertyTypeReorder> shifts;
}
