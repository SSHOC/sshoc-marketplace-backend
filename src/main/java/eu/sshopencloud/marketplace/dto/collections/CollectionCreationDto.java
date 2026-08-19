package eu.sshopencloud.marketplace.dto.collections;

import eu.sshopencloud.marketplace.dto.items.ItemMediaCore;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyCore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO containing information about collection that is about to be created
 */
@Data
@NoArgsConstructor
public class CollectionCreationDto {

    private String title;

    private String description;

    private List<String> containedItems = new ArrayList<>();

    private boolean visible;

    private List<PropertyCore> properties;

    private ItemMediaCore thumbnail;
}
