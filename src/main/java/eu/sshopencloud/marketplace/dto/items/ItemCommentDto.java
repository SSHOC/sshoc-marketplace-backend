package eu.sshopencloud.marketplace.dto.items;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.dto.auth.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class ItemCommentDto {

    private Long id;

    private String body;

    private UserDto creator;

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    private ZonedDateTime dateCreated;

    @Schema(type="string", pattern = ApiDateTimeFormatter.outputDateTimePattern, example = ApiDateTimeFormatter.outputDateTimeExample)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.outputDateTimePattern)
    @Nullable
    private ZonedDateTime dateLastUpdated;

}
