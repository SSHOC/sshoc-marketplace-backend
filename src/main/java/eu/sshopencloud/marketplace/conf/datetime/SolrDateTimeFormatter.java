package eu.sshopencloud.marketplace.conf.datetime;

import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class SolrDateTimeFormatter {

    public final String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);

    public String formatDateTime(ZonedDateTime date) {
        return date.format(dateTimeFormatter);
    }

}
