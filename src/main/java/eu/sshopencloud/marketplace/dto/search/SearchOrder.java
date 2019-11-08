package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchOrder {

    NAME,

    MODIFIED_ON;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
