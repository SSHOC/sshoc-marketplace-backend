package eu.sshopencloud.marketplace.dto.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class PropertyTypeCore {

    private String label;
    private PropertyTypeClass type;

    private List<String> allowedVocabularies;
}
