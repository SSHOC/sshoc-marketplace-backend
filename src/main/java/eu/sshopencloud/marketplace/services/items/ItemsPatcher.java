package eu.sshopencloud.marketplace.services.items;

import eu.sshopencloud.marketplace.dto.items.*;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

class ItemsPatcher {

    // static Jackson ObjectMapper so we don't have to keep creating
    // and configuring one for each patch operaion
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    // a TypeReference we can reusue for mapping an object to a simple
    // String to Object Map
    private static TypeReference<Map<String, Object>> TO_MAP = new TypeReference<Map<String, Object>>() {};

    static {
        // because of the differences between DTO and Core versions of the
        // same object we want to be able to ignire missing values when
        // reading back an encoded version
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // by default Jackson won't serialise the old Java 8 date/time classes which includes ZonedDateTime
        // which is used to store created/updated dates. This adds support for that so we can correctly
        // round trip them when converting between the DTO and Core versions.
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
    }

    static void patchItemCore(ItemDto currentItemDto, ItemDto firstIngestDto, ItemCore itemCore) {

        try {
        // convert the current item to a map (i.e. all the fields from the item become
        // keys in the map)
        Map<String, Object> dto = objectMapper.convertValue(currentItemDto, TO_MAP);

        // convert the patch into a map in the same way
        Map<String, Object> core = objectMapper.convertValue(itemCore, TO_MAP);
        
        // any fields in the ItemCore that are null should be ignored as these are
        // just the fields that are not specified in the patch
        core.values().removeIf(Objects::isNull);

        // note that in the previous approach empty lists in the patch were treated
        // the same as null values. This doesn't match the semantics of PATCH, as defined
        // in RFC 5789, because it means that you could never patch an object to remove
        // all elements of a list because passing in an empty list would result in the
        // values from the current item being used instead. If the prior behaviour
        // is necessary then it can be implemented by uncommenting the following line
        // and add an import statement for java.util.Collection
        // core.values().removeIf(e -> e instanceof Collection && ((Collection<?>)e).isEmpty());

        // patch the original object by replacing all the fields in the map based
        // version with those from the patch that has been supplied
        dto.putAll(core);

        // handle any special cases based on if there is a previously ingested
        // version of the DTO (I don't really understand the logic of what this
        // is trying to achieve but it matches the logic in the previous version
        // of this method). Note that the logic in determinePatchValue seems to
        // result in updates that violate the semantics of RFC 5789 in that
        // under some circumstances the current value will be retained even when
        // when a new value is provided. Whilst that may make some sense for
        // the version field (should that even be patchable) I think it would
        // be wrong (or at least counter intuitive) for both label and
        // description fields.
        boolean previouslyIngested = Objects.nonNull(firstIngestDto);
        dto.put("label",
            determinePatchValue(
                currentItemDto.getLabel(),
                !previouslyIngested ? null : firstIngestDto.getLabel(),
                itemCore.getLabel(),
                previouslyIngested));

        dto.put("description",
            determinePatchValue(
                currentItemDto.getDescription(),
                !previouslyIngested ? null : firstIngestDto.getDescription(),
                itemCore.getDescription(),
                previouslyIngested));

        dto.put("version",
            determinePatchValue(
                currentItemDto.getVersion(),
                !previouslyIngested ? null : firstIngestDto.getVersion(),
                itemCore.getVersion(),
                previouslyIngested));

        // we now have a map instance holding values for all the fields of
        // the fully updated object. the final step is to use these values
        // to update the ItemCore instance we were passed. This would be
        // a simple case of using ObjectMapper.convertValue() to create a
        // new instance of ItemCore if we were returning the updated object.
        // Unfortunately we aren't returning an instance rather we need to update
        // the object passed in by reference. Fortunately we can still do this
        // using Jackson for all the heavy lifting....
        try {
            // first we convert the map into JSON stroed in a byte array
            byte[] bytes = objectMapper.writeValueAsBytes(dto);
            
            // create a reader that will update the original object
            // we want to patch rather than creating a new instance
            ObjectReader reader = objectMapper.readerForUpdating(itemCore);

            // finally we patch the object by reading back the updated values
            // from the byte array we generated above.
            reader.readValue(bytes);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
            // this should be impossible because both ItemDto and ItemCore are
            // clearly serializable to JSON as both are returned/consumed by
            // numerous endpoints within the backend, and as all values in the
            // map have come from one or other type then logically the map
            // itself must also be serializbale

            // we will raise an exception though just in case, as if we ignore
            // this and it does happen then the result would be that itemCore
            // would not be correctly updated which could result in data loss
            throw new RuntimeException("Unable to serialize item field values", jpe);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // this should also be impossible because if we get as far as reading
            // from the byte array then it must hold valid JSON. The only issue
            // would be (I think) if ItemCore had a field with the same name as
            // ItemDto but they had very different types. I think that would
            // probably trigger a JSON deserialization rutime exception rather
            // than an IOException though

            // either way we will re-throw the exception so that the  error propogates
            // back up rather than silently hiding it which would result in itemCore
            // not being updated correctly and potential data loss
            throw new RuntimeException("unable to update ItemCore instance", ioe);
        }}
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String determinePatchValue(String currentValue, String firstIngestValue, String newValue, boolean previouslyIngested) {
        if (previouslyIngested && !StringUtils.isBlank(newValue)) {

            // nothing has changed -> new value has the proper value
            if (Objects.isNull(currentValue) && Objects.isNull(firstIngestValue)) {
                return newValue;
            }
            // first ingest has been modified along the way as it is the sane as new value, so we need to take a modified version,
            // and we know current value is not null from previous condition
            if (Objects.isNull(firstIngestValue) && !Objects.equals(newValue, currentValue)) {
                return currentValue;
            }
            // first ingest has been modified along the way and new value is the same as current value, so we need to take a modified version
            // and we know firstIngestValue is not null from previous condition
            if (Objects.isNull(currentValue) && !Objects.equals(newValue, firstIngestValue)) {
                return newValue;
            }
            // change at source happened, so we take new value
            if (Objects.nonNull(firstIngestValue) && Objects.nonNull(currentValue) && Objects.equals(currentValue, firstIngestValue) && !Objects.equals(newValue, currentValue)) {
                return newValue;
            }
            // conflict, so we take current value
            if (Objects.nonNull(firstIngestValue) && Objects.nonNull(currentValue) && !Objects.equals(currentValue, firstIngestValue) && !Objects.equals(newValue, currentValue) && !Objects.equals(newValue, firstIngestValue)) {
                return currentValue;
            }
        } else {
            // if no new value is provided then keep current value as we are in patch mode
            if (StringUtils.isBlank(newValue)) {
                return currentValue;
            }
        }

        return newValue;
    }
}
