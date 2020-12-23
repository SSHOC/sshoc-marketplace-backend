package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemStatus {
    DRAFT,
    INGESTED,
    SUGGESTED,
    APPROVED,
    DISAPPROVED,
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
