package eu.sshopencloud.marketplace.conf.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.MalformedURLException;
import java.net.URL;

@Converter
public class UrlConverter implements AttributeConverter<URL, String> {

    @Override
    public String convertToDatabaseColumn(URL attribute) {
        return (attribute != null) ? attribute.toString() : null;
    }

    @Override
    public URL convertToEntityAttribute(String dbData) {
        try {
            return (dbData != null) ? new URL(dbData) : null;
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Invalid url syntax: %s", dbData), e);
        }
    }
}
