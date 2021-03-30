package eu.sshopencloud.marketplace.conf.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.nio.file.Path;


@Converter
public class FilePathConverter implements AttributeConverter<Path, String> {

    @Override
    public String convertToDatabaseColumn(Path attribute) {
        return (attribute != null) ? attribute.toString() : null;
    }

    @Override
    public Path convertToEntityAttribute(String dbData) {
        return (dbData != null) ? Path.of(dbData) : null;
    }
}
