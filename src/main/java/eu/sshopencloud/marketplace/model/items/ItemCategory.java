package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {

    TOOL,

    TRAINING_MATERIAL;

    @JsonValue
    public String getMeters() {
        return name().replace('_', '-').toLowerCase();
    }

}
