package eu.sshopencloud.marketplace.dto.collections;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto containing information about suggestion for collection
 */
@Data
@NoArgsConstructor
public class CollectionSuggestionStatusActionDto {

    public enum ACTION {
        APPROVE,
        REJECT
    }
    private ACTION action;
}
