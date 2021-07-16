package eu.sshopencloud.marketplace.dto.auth;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserOrder {

    USERNAME(true),

    DATE(false);

    private boolean asc;

    UserOrder(boolean asc) {
        this.asc = asc;
    }

    public boolean isAsc() {
        return this.asc;
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }
}
