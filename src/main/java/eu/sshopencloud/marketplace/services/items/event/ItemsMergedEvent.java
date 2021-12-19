package eu.sshopencloud.marketplace.services.items.event;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemsMergedEvent {

    private String newPersistentId;

    private List<String> mergedPersistentIds;

}
