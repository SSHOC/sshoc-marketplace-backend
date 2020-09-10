package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {
    TOOL_OR_SERVICE,
    TRAINING_MATERIAL,
    PUBLICATION,
    DATASET,
    WORKFLOW,
    STEP;

    @Deprecated
    public static final String OBJECT_TYPE_PROPERTY_TYPE_CODE = "object-type";

    @Deprecated
    public static final String OBJECT_TYPE_VOCABULARY_CODE = "object-type";

    public static ItemCategory[] indexedCategories() {
        return new ItemCategory[] {TOOL_OR_SERVICE, TRAINING_MATERIAL, PUBLICATION, DATASET, WORKFLOW};
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
