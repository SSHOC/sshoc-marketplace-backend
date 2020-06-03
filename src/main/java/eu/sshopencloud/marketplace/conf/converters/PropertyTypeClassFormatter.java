package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.vocabularies.PropertyTypeClass;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class PropertyTypeClassFormatter implements Formatter<PropertyTypeClass>  {

    @Override
    public String print(PropertyTypeClass object, Locale locale) {
        return object.getValue();
    }

    public PropertyTypeClass parse(String text, Locale locale) throws ParseException {
        try {
            return PropertyTypeClass.valueOf(text.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "order" + "'!", 0);
        }
    }

}
