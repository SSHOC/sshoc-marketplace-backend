package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemCategory {
    TOOL_OR_SERVICE("Tools & Services"),
    TRAINING_MATERIAL("Training Materials"),
    PUBLICATION("Publications"),
    DATASET("Datasets"),
    WORKFLOW("Workflows"),
    STEP("Steps");

    private final String label;

    ItemCategory(String label) {
        this.label = label;
    }


    public static ItemCategory[] indexedCategories() {
        return new ItemCategory[] {TOOL_OR_SERVICE, TRAINING_MATERIAL, PUBLICATION, DATASET, WORKFLOW, STEP};
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getValue();
    }

}
