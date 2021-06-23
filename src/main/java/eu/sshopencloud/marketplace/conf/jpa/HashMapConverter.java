package eu.sshopencloud.marketplace.conf.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;

public class HashMapConverter implements AttributeConverter<Map<String, String>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Map to JSON conversion error", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (final IOException e) {
            throw new RuntimeException("JSON to Map conversion error", e);
        }
    }

}