package com.qa.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Interface providing default methods for JSON serialization and deserialization.
 *
 * <p>This interface defines methods for converting objects to and from JSON using
 * the Jackson library. Classes that implement this interface will automatically
 * inherit the ability to serialize themselves to JSON and deserialize from JSON.</p>
 *
 * @param <T> the type of the implementing class, allowing for proper casting during deserialization
 */
public interface JsonSerializable<T> {

    @SuppressWarnings("unchecked")
    default T fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (T) mapper.readValue(json, this.getClass());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String str = "";
        try {
            str = mapper.writeValueAsString(this);
        } catch (Exception exception) {
            throw new RuntimeException();
        }
        return str;
    }
}
