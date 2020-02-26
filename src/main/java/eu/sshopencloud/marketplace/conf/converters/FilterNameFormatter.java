package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.FilterName;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class FilterNameFormatter implements Formatter<FilterName> {

    @Override
    public String print(FilterName object, Locale locale) {
        return object.getValue();
    }

    public FilterName parse(String text, Locale locale) throws ParseException {
        try {
            return FilterName.valueOf(text.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "filter name" + "'!", 0);
        }
    }

}
