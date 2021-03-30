package eu.sshopencloud.marketplace.dto.items;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.domain.media.dto.MediaDetails;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.dto.licenses.LicenseDto;
import eu.sshopencloud.marketplace.dto.sources.SourceBasicDto;
import eu.sshopencloud.marketplace.dto.sources.SourceDto;
import eu.sshopencloud.marketplace.dto.vocabularies.PropertyDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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

    private List<ItemExternalIdDto> externalIds;

    private List<String> accessibleAt;

    private SourceBasicDto source;

    private String sourceItemId;

    private List<RelatedItemDto> relatedItems;

    private List<ItemMediaDto> media;

    private ItemThumbnailId thumbnail;

    private UserDto informationContributor;

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    private ZonedDateTime lastInfoUpdate;

    private ItemStatus status;

    private List<ItemBasicDto> olderVersions;

    private List<ItemBasicDto> newerVersions;

}
