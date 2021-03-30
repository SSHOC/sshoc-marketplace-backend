package eu.sshopencloud.marketplace.conf.converters;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;


abstract class BaseEnumFormatter<T extends Enum<T>> implements Formatter<T> {

    @Override
    public T parse(String text, Locale locale) throws ParseException {
        try {
            return toEnum(text.toUpperCase().replace("-", "_"));
        }
        catch (Exception e) {
            throw new ParseException(String.format("Incorrect value '%s' for enum '%s'", text, getEnumName()), 0);
        }
    }

    @Override
    public String print(T object, Locale locale) {
        return object.name().replace("_", "-").toLowerCase();
    }

    protected abstract T toEnum(String enumValue);
    protected abstract String getEnumName();
}
