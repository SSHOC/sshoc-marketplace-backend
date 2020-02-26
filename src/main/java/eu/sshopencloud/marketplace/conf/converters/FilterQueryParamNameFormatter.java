package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.FilterQueryParamName;
import eu.sshopencloud.marketplace.model.items.ItemCategory;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class FilterQueryParamNameFormatter implements Formatter<FilterQueryParamName> {

    @Override
    public String print(FilterQueryParamName object, Locale locale) {
        return object.getValue();
    }

    @Override
    public FilterQueryParamName parse(String text, Locale locale) throws ParseException {
        try {
            String fname = text.toUpperCase().replace('-', '_');
            return FilterQueryParamName.valueOf(fname.substring(2, fname.length()));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "filter param name" + "'!", 0);
        }
    }

}
