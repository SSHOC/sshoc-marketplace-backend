package eu.sshopencloud.marketplace.conf.datetime;

import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class ApiDateTimeFormatter {

    public final String inputDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS]XX";

    public final String inputDateTimeExample = "2000-02-29T20:02:00+0000";

    public final String outputDateTimePattern = "yyyy-MM-dd'T'HH:mm:ssZ";

    public final String outputDateTimeExample = "2000-02-29T20:02:00+0000";


    public DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(outputDateTimePattern);

    public String formatDateTime(ZonedDateTime date) {
        return date.format(dateTimeFormatter);
    }

}
