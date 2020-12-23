package eu.sshopencloud.marketplace.model.items;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;


@UtilityClass
public class PersistentId {
    private static final String PERSISTENT_ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int PERSISTENT_ID_LENGTH = 6;

    public String generated() {
        return RandomStringUtils.random(PERSISTENT_ID_LENGTH, PERSISTENT_ID_CHARS);
    }
}
