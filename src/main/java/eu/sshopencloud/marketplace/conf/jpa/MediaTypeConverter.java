package eu.sshopencloud.marketplace.conf.jpa;

import org.springframework.http.MediaType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


@Converter
public class MediaTypeConverter implements AttributeConverter<MediaType, String> {

    @Override
    public String convertToDatabaseColumn(MediaType attribute) {
        return (attribute != null) ? attribute.toString() : null;
    }

    @Override
    public MediaType convertToEntityAttribute(String dbData) {
        return MediaType.parseMediaType(dbData);
    }
}
