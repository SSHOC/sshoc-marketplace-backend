package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.search.SearchOrder;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class SearchOrderFormatter implements Formatter<SearchOrder> {

    @Override
    public String print(SearchOrder object, Locale locale) {
        return object.getValue();
    }

    public SearchOrder parse(String text, Locale locale) throws ParseException {
        try {
            return SearchOrder.valueOf(text.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "order" + "'!", 0);
        }
    }

}
