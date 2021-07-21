package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.dto.vocabularies.ConceptId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemMediaCore {
    private MediaDetailsId info;
    private String caption;
    private ConceptId concept;
}
