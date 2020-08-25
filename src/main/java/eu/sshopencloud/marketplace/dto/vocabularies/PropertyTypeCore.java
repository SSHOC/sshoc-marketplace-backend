package eu.sshopencloud.marketplace.dto.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class PropertyTypeCore {

    private String label;
    private PropertyTypeClass type;
    private int ord;
}
