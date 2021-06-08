package eu.sshopencloud.marketplace.dto.auth;

import eu.sshopencloud.marketplace.model.auth.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserCore {

    private String username;

    private String displayName;

    private String password;

    private UserRole role;

    private String email;

}
