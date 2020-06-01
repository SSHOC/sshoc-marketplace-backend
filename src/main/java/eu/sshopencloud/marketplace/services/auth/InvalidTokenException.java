package eu.sshopencloud.marketplace.services.auth;

public class InvalidTokenException extends Exception {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

}
