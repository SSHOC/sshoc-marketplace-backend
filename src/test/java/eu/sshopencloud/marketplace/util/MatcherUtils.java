package eu.sshopencloud.marketplace.util;

import lombok.experimental.UtilityClass;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


public class MatcherUtils {

    public static Matcher<Number> equalValue(long value) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Number item) {
                return (item.longValue() == value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("is not equal to %d", value));
            }
        };
    }
}
