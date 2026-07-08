package eu.sshopencloud.marketplace.dto.collections;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CollectionItemDto {

    private Long id;

    private String persistentId;

    private boolean isSuggested;

    private String comment;
}
