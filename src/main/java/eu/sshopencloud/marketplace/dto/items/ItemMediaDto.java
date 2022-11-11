package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.dto.vocabularies.ConceptBasicDto;
import eu.sshopencloud.marketplace.model.items.ItemMediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemMediaDto {
    private MediaDetails info;
    private String caption;
    private ConceptBasicDto concept;
}
