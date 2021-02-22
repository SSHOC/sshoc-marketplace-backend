package eu.sshopencloud.marketplace.controllers.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.util.Optional;

@UtilityClass
public class MimeTypeUtils {

    public Optional<MediaType> parseMimeType(String mimeType) {
        if (StringUtils.isBlank(mimeType))
            return Optional.empty();

        return Optional.of(MediaType.parseMediaType(mimeType));
    }
}
