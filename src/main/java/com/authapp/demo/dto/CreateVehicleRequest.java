package com.authapp.demo.dto;

public class CreateVehicleRequest {
    private String plate;
    private String model;
    private Long userId;

    // Default constructor
    public CreateVehicleRequest() {}

    // Constructor with all fields
    public CreateVehicleRequest(String plate, String model, Long userId) {
        this.plate = plate;
        this.model = model;
        this.userId = userId;
    }

    // Getters and setters
    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
} 