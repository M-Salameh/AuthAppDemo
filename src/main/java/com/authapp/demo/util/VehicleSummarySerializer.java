package com.authapp.demo.util;

import com.authapp.demo.entity.Vehicle;
import com.authapp.demo.entity.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * Custom JSON serializer for Vehicle entities.
 * Serializes only the plate and model fields of a Vehicle for summary views.
 */
public class VehicleSummarySerializer extends JsonSerializer<Vehicle> {
    /**
     * Serializes a Vehicle object to JSON, including only the plate and model fields.
     *
     * @param vehicle the Vehicle object to serialize
     * @param gen the JSON generator
     * @param serializers the serializer provider
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void serialize(Vehicle vehicle, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("plate", vehicle.getPlate());
        gen.writeStringField("model", vehicle.getModel());
        gen.writeEndObject();
    }
} 