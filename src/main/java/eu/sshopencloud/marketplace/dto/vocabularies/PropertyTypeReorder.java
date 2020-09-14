package eu.sshopencloud.marketplace.dto.vocabularies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeReorder {

    @NotNull
    private String code;

    private int ord;
}
