package eu.sshopencloud.marketplace.dto.auth;

import eu.sshopencloud.marketplace.model.auth.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

    private Long id;

    private String username;

    private boolean enabled;

    private UserRole role;

}
