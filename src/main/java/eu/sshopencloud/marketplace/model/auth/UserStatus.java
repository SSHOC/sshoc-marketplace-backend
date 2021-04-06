package eu.sshopencloud.marketplace.model.auth;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {

    DURING_REGISTRATION,
    ENABLED,
    LOCKED;


    @JsonValue
    public String getValue() {
        return name().replace("_", "-").toLowerCase();
    }

    public static UserStatus of(String status) {
        return UserStatus.valueOf(status.toUpperCase().replace("-", "_"));
    }

    @Override
    public String toString() {
        return getValue();
    }


}
