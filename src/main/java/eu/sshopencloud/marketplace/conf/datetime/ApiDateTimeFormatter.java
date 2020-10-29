package eu.sshopencloud.marketplace.conf.datetime;

import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class ApiDateTimeFormatter {

    public final String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ssZ";

    public final String dateTimeExample = "2000-02-29'T'20:02:00+0000";

    public DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);

    public String formatDateTime(ZonedDateTime date) {
        return date.format(dateTimeFormatter);
    }

}
