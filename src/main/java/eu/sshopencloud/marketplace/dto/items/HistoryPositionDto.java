package eu.sshopencloud.marketplace.dto.items;


import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class HistoryPositionDto {
    private Long id;

    private ItemCategory category;

    private String label;

    private String version;

    private String persistentId;

    private ItemStatus status;

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    private ZonedDateTime lastInfoUpdate;

    private UserDto informationContributor;

}
