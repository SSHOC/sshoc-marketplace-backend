package eu.sshopencloud.marketplace.services.auth;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class OidcAuthenticationProcessingException extends AuthenticationException {

    private final String errorCode;

    public OidcAuthenticationProcessingException(String errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

}
