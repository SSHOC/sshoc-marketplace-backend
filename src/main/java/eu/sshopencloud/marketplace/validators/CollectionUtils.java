package eu.sshopencloud.marketplace.validators;

import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.stream.StreamSupport;

@UtilityClass
public class CollectionUtils {

    public static boolean isAllNulls(Iterable<?> array) {
        return StreamSupport.stream(array.spliterator(), true).allMatch(o -> Objects.isNull(o));
    }
}
