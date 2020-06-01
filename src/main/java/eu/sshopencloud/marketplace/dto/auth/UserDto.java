package eu.sshopencloud.marketplace.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.model.auth.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class UserDto {

    private Long id;

    private String username;

    private String displayName;

    private boolean enabled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ApiDateTimeFormatter.dateTimePattern)
    private ZonedDateTime registrationDate;

    private UserRole role;

    private String email;

}
