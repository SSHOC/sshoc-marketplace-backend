package eu.sshopencloud.marketplace.conf.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class OidcAuthenticationProcessingException extends AuthenticationException {
    public OidcAuthenticationProcessingException(String msg, Throwable t) {
        super(msg, t);
    }

    public OidcAuthenticationProcessingException(String msg) {
        super(msg);
    }
}