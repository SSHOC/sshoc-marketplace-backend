package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VersionedItemStatus {
    DRAFT,
    INGESTED,
    SUGGESTED,
    REVIEWED,
    REFUSED,
    MERGED,
    DELETED;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }
}
