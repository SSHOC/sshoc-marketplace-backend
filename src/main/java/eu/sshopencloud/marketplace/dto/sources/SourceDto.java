package eu.sshopencloud.marketplace.dto.sources;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SourceDto extends SourceBasicDto {

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    @Nullable
    private ZonedDateTime lastHarvestedDate;

}
