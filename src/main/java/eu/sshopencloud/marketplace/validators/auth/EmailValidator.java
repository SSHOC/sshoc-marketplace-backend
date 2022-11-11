package eu.sshopencloud.marketplace.validators.auth;

import lombok.experimental.UtilityClass;
import java.util.regex.Pattern;

@UtilityClass
public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    public boolean isValid(String email) {
        return  (EMAIL_PATTERN.matcher(email).matches());
    }

}
