package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.items.ItemCategory;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class ItemCategoryFormatter implements Formatter<ItemCategory> {

    @Override
    public String print(ItemCategory object, Locale locale) {
        return object.getValue();
    }

    public ItemCategory parse(String text, Locale locale) throws ParseException {
        try {
            return ItemCategory.valueOf(text.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "category" + "'!", 0);
        }
    }

}
