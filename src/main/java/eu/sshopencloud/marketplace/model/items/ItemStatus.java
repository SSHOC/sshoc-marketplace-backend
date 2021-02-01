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

    public static ItemStatus of(String status) {
        return ItemStatus.valueOf(status.toUpperCase().replace("-", "_"));
    }

    @Override
    public String toString() {
        return getValue();
    }
}
