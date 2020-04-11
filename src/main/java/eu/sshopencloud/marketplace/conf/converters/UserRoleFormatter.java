package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.auth.UserRole;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

public class UserRoleFormatter implements Formatter<UserRole> {

    @Override
    public String print(UserRole object, Locale locale) {
        return object.getValue();
    }

    public UserRole parse(String text, Locale locale) throws ParseException {
        try {
            return UserRole.valueOf(text.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            throw new ParseException("Incorrect value '" + text + "' for enum '" + "order" + "'!", 0);
        }
    }

}
