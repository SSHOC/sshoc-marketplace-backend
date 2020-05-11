package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemStatus {

    INGESTED,

    REVIEWED,

    DEPRECATED;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
