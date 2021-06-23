package eu.sshopencloud.marketplace.dto.items;

import eu.sshopencloud.marketplace.dto.sources.SourceBasicDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ItemDto extends ItemExtBasicDto {

    private String description;

    private List<ItemContributorDto> contributors;

    private List<PropertyDto> properties;

    private List<ItemExternalIdDto> externalIds;

    private List<String> accessibleAt;

    private SourceBasicDto source;

    private String sourceItemId;

    private List<RelatedItemDto> relatedItems;

    private List<ItemMediaDto> media;

    private ItemThumbnailId thumbnail;

}
