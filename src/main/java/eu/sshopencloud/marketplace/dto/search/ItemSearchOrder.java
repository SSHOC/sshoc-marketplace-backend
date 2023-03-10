package eu.sshopencloud.marketplace.dto.search;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemSearchOrder {

    SCORE(false),

    LABEL(true),

    MODIFIED_ON(false);

    private final boolean asc;

    ItemSearchOrder(boolean asc) {
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
