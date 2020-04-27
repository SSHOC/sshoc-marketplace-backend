package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {

    TOOL,

    TRAINING_MATERIAL,

    DATASET,

    ACTIVITY,

    WORKFLOW;

    public static final String OBJECT_TYPE_PROPERTY_TYPE_CODE = "object-type";

    public static final String OBJECT_TYPE_VOCABULARY_CODE = "object-type";

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
