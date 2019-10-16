package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {

    TOOL,

    TRAINING_MATERIAL,

    VOCABULARY;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

}
