package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.dto.items.ItemContributorDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchItem {

    private Long id;

    private String persistentId;

    private String label;

    private String description;

    private List<ItemContributorDto> contributors;

    private List<PropertyDto> properties;

    private ItemCategory category;

    private ItemStatus status;

    private String owner;

    private String lastInfoUpdate;

}
