package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {

    TOOL,

    TRAINING_MATERIAL,

    DATASET;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
