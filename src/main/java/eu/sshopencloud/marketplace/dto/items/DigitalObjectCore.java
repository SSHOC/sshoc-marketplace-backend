package eu.sshopencloud.marketplace.dto.items;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DigitalObjectCore extends ItemCore {

    @Schema(type="string", pattern = ApiDateTimeFormatter.inputDateTimePattern, example = ApiDateTimeFormatter.inputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.inputDateTimePattern)
    private ZonedDateTime dateCreated;

    @Schema(type="string", pattern = ApiDateTimeFormatter.inputDateTimePattern, example = ApiDateTimeFormatter.inputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.inputDateTimePattern)
    private ZonedDateTime dateLastUpdated;

}
