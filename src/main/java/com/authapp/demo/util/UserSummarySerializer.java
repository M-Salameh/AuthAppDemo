package com.authapp.demo.util;

import com.authapp.demo.entity.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * Custom JSON serializer for User entities.
 * Serializes only the id and username fields of a User for summary views.
 */
public class UserSummarySerializer extends JsonSerializer<User> {
    /**
     * Serializes a Vehicle object to JSON, including only the plate and model fields.
     *
     * @param user the User object to serialize
     * @param gen the JSON generator
     * @param serializers the serializer provider
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void serialize(User user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", user.getId());
        gen.writeStringField("username", user.getUsername());
        gen.writeEndObject();
    }
} 