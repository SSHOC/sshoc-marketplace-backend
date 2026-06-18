package eu.sshopencloud.marketplace.dto.collections;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.items.ItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO containing general information about collection
 */
@Data
@NoArgsConstructor
public class CollectionDto {

    private Long id;

    private String title;

    private String description;

    private boolean visible;

    private List<CollectionItemDto> collectionItems;

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    private ZonedDateTime createdAt;

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    private ZonedDateTime updatedAt;

}
