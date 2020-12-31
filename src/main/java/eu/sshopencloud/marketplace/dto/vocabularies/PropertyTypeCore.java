package eu.sshopencloud.marketplace.dto.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeCore {

    private String code;
    private String label;
    private PropertyTypeClass type;

    private List<String> allowedVocabularies;
}
