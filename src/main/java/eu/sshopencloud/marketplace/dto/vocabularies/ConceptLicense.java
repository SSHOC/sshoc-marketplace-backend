package eu.sshopencloud.marketplace.dto.vocabularies;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConceptLicense {

    SOFTWARE_LICENSE;

    @JsonValue
    public String getValue() {
        return name().replace('_', '-').toLowerCase();
    }

    @Override
    public String toString() {
        return getValue();
    }
}
