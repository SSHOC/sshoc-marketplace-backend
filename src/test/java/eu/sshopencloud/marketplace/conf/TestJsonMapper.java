package eu.sshopencloud.marketplace.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.sshopencloud.marketplace.conf.datetime.ZonedDateTimeDeserializer;
import eu.sshopencloud.marketplace.conf.datetime.ZonedDateTimeSerializer;
import lombok.experimental.UtilityClass;

import java.time.ZonedDateTime;

@UtilityClass
public class TestJsonMapper {

    public ObjectMapper serializingObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        javaTimeModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

}
