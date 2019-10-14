package eu.sshopencloud.marketplace.model.items;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrainingMaterialType {

    PAPER,

    TUTORIAL,

    ONLINE_COURSE,

    WEBINAR,

    BLOG;

    @JsonValue
    public String getMeters() {
        return name().replace('_', '-').toLowerCase();
    }

}
