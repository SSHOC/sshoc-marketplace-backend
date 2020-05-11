package eu.sshopencloud.marketplace.dto.items;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.licenses.LicenseDto;
import eu.sshopencloud.marketplace.dto.sources.SourceBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ItemDto extends ItemBasicDto {

    private String description;

    private List<LicenseDto> licenses;

    private List<ItemContributorDto> contributors;

    private List<PropertyDto> properties;

    private String accessibleAt;

    private SourceBasicDto source;

    private String sourceItemId;

    private List<RelatedItemDto> relatedItems;

    private List<UserDto> informationContributors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private ZonedDateTime lastInfoUpdate;

    private List<ItemCommentDto> comments;

    private List<ItemBasicDto> olderVersions;

    private List<ItemBasicDto> newerVersions;

}
