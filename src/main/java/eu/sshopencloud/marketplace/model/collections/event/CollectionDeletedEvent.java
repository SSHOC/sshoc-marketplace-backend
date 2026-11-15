package eu.sshopencloud.marketplace.model.collections.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionDeletedEvent {

    private long collectionId;

    private UUID thumbnailMediaId;
}
