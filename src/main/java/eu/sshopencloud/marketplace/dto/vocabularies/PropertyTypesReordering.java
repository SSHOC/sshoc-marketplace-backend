package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypesReordering {

    @NotNull
    private List<PropertyTypeReorder> shifts;
}
