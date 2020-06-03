package eu.sshopencloud.marketplace.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OAuthRegistrationData {

    private Long id;

    private String displayName;

    private String email;

    private boolean acceptedRegulations;

}
