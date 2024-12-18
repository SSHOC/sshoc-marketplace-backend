package eu.sshopencloud.marketplace.dto.auth;

import eu.sshopencloud.marketplace.model.auth.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class UserCore {

    @NotNull
    private String username;

    private String displayName;

    @NotNull
    private String password;

    private UserRole role;

    @NotNull
    private String email;

}
