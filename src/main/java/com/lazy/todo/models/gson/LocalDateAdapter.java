package com.lazy.todo.models.gson;
//this allows us to adapt LocalDates to and from JSON for serialization
import com.google.gson.*;
import io.swagger.v3.core.util.Json;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDate <--> JSON support
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    //From JSON
    @Override
    public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return LocalDate.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    //To JSON
    @Override
    public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(
                localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
    }
}