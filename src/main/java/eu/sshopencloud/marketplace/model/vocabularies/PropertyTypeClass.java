package eu.sshopencloud.marketplace.model.vocabularies;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyTypeClass {

    CONCEPT,
    STRING,
    URL,
    INT,
    FLOAT,
    DATE;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
