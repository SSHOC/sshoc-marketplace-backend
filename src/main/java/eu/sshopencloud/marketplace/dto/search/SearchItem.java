package eu.sshopencloud.marketplace.dto.search;

import eu.sshopencloud.marketplace.dto.items.ItemContributorDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SearchItem extends SearchItemBasic {

    private String description;

    private List<ItemContributorDto> contributors;

    private List<PropertyDto> properties;

    private ItemStatus status;

    private String owner;

}
