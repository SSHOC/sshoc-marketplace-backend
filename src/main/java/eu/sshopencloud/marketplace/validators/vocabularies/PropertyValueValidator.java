package eu.sshopencloud.marketplace.validators.vocabularies;

import eu.sshopencloud.marketplace.conf.datetime.ApiDateTimeFormatter;
import eu.sshopencloud.marketplace.model.vocabularies.PropertyType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.format.DateTimeParseException;


@Component
public class PropertyValueValidator {

    public boolean validate(String value, PropertyType propertyType, Errors errors) {
        if (StringUtils.isBlank(value)) {
            errors.rejectValue("value", "field.required", "Property value is required.");
            return false;
        }

        switch (propertyType.getType()) {
            case STRING:
                return true;
            case INT:
                return validateInteger(value, errors);
            case FLOAT:
                return validateFloat(value, errors);
            case URL:
                return validateUrl(value, errors);
            case DATE:
                return validateDate(value, errors);
            case BOOLEAN:
                return validateBoolean(value, errors);
        }

        return false;
    }

    private boolean validateInteger(String value, Errors errors) {
        try {
            new BigInteger(value);
        }
        catch (NumberFormatException e) {
            errors.rejectValue(
                    "value", "field.invalid",
                    String.format("The value is not an integer: %s", e.getMessage())
            );
            return false;
        }

        return true;
    }

    private boolean validateFloat(String value, Errors errors) {
        try {
            new BigDecimal(value);
        }
        catch (NumberFormatException e) {
            errors.rejectValue(
                    "value", "field.invalid",
                    String.format("The value is not an integer: %s", e.getMessage())
            );
            return false;
        }

        return true;
    }

    private boolean validateUrl(String value, Errors errors) {
        try {
            new URL(value).toURI();
        }
        catch (MalformedURLException | URISyntaxException e) {
            errors.rejectValue(
                    "value", "field.invalid",
                    String.format("Property value '%s' is an invalid URL", value)
            );
            return false;
        }

        return true;
    }

    private boolean validateDate(String value, Errors errors) {

        try {
            ApiDateTimeFormatter.INPUT_DATE_FORMATTER.parse(value);
        }
        catch (DateTimeParseException e) {
            try {
                ApiDateTimeFormatter.SIMPLE_DATE_FORMATTER.parse(value);
            }
            catch (DateTimeParseException e2) {
                errors.rejectValue(
                        "value", "field.invalid",
                        "Invalid date format. Only 'yyyy-MM-ddTHH:mm:ss[.SSS]+XXXX' or 'yyyy-MM-dd' are supported."
                );
                return false;
            }
        }

        return true;
    }

    private boolean validateBoolean(String value, Errors errors) {
        if (!(value.equals("TRUE") || value.equals("FALSE") || value.equals("NULL"))) {
            errors.rejectValue(
                    "value", "field.invalid","Only TRUE or FALSE or NULL are allowed."
            );
            return false;
        }

        return true;
    }

}
