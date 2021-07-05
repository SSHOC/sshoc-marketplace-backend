package eu.sshopencloud.marketplace.dto.sources;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SourceOrder {

    NAME(true),

    HARVEST_DATE(false);

    private boolean asc;

    SourceOrder(boolean asc) {
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
