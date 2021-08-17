package eu.sshopencloud.marketplace.dto.vocabularies;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyTypeCore {

    @NotNull
    private String code;
    private String label;
    private PropertyTypeClass type;

    private String groupName;
    private boolean hidden;

    private Integer ord;

    private List<String> allowedVocabularies;
}
