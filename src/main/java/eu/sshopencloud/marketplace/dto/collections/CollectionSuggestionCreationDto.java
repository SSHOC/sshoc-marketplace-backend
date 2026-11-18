package eu.sshopencloud.marketplace.dto.collections;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto containing information about suggestion for collection
 */
@Data
@NoArgsConstructor
public class CollectionSuggestionCreationDto {

    private String itemPersistentId;

    private String comment;
}
