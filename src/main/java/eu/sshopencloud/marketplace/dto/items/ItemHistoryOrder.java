package eu.sshopencloud.marketplace.dto.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemHistoryOrder {

    DATE(false);

    private boolean asc;

    ItemHistoryOrder(boolean asc) {
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
