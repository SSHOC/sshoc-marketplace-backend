package eu.sshopencloud.marketplace.domain.media;

import com.fasterxml.jackson.annotation.JsonValue;


public enum MediaCategory {
    IMAGE,
    VIDEO,
    EMBED,
    OBJECT,
    THUMBNAIL;

    @JsonValue
    public String getValue() {
        return name().replace("_", "-").toLowerCase();
    }

    public static MediaCategory of(String category) {
        return MediaCategory.valueOf(category.toUpperCase().replace("-", "_"));
    }

    @Override
    public String toString() {
        return getValue();
    }

}
