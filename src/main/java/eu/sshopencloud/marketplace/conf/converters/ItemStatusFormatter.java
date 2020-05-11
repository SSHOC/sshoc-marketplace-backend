package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import eu.sshopencloud.marketplace.model.items.ItemStatus;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class ItemStatusFormatter implements Formatter<ItemStatus> {

    @Override
    public String print(ItemStatus object, Locale locale) {
        return object.getValue();
    }

    public ItemStatus parse(String text, Locale locale) throws ParseException {
        try {
            return ItemStatus.valueOf(text.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "category" + "'!", 0);
        }
    }

}
