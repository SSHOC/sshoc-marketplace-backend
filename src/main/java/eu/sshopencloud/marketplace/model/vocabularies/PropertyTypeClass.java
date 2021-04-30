package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyTypeClass {

    CONCEPT("_ss"),
    STRING("_ss"),
    URL("_ss"),
    INT("_ls"),
    FLOAT("_ds"),
    DATE("_dts");

    private String dynamicFieldIndexTypeSuffix;

    PropertyTypeClass(String dynamicFieldIndexTypeSuffix) {
        this.dynamicFieldIndexTypeSuffix = dynamicFieldIndexTypeSuffix;
    }

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

    public String getDynamicFieldIndexTypeSuffix() {
        return dynamicFieldIndexTypeSuffix;
    }

}
