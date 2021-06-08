package eu.sshopencloud.marketplace.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgottenPasswordData {

    private String newPassword;

    private String verifiedPassword;

}
