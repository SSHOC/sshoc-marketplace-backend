package eu.sshopencloud.marketplace.domain.media.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum MediaCategory {
    IMAGE,
    VIDEO,
    OBJECT,
    THUMBNAIL;

    @JsonValue
    public String getValue() {
        return name().replace("_", "-").toLowerCase();
    }

    @JsonCreator
    public static MediaCategory of(String category) {
        return MediaCategory.valueOf(category.toUpperCase().replace("-", "_"));
    }
}
