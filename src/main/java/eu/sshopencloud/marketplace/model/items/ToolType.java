package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ToolType {

    SOFTWARE,

    SERVICE;

    @JsonValue
    public String getMeters() {
        return name().replace('_', '-').toLowerCase();
    }

}
