package com.authapp.demo.util;

import com.authapp.demo.entity.Vehicle;
import com.authapp.demo.entity.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class VehicleSummarySerializer extends JsonSerializer<Vehicle> {
    @Override
    public void serialize(Vehicle vehicle, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("plate", vehicle.getPlate());
        gen.writeStringField("model", vehicle.getModel());
        gen.writeEndObject();
    }
} 