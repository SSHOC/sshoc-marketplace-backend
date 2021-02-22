package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.domain.media.dto.MediaCategory;


public class MediaCategoryFormatter extends BaseEnumFormatter<MediaCategory> {
    private static final String MEDIA_CATEGORY_NAME = "media-category";

    @Override
    protected MediaCategory toEnum(String enumValue) {
        return MediaCategory.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return MEDIA_CATEGORY_NAME;
    }
}
