package com.authapp.demo.dto;

/**
 * Data Transfer Object for creating or updating a Vehicle.
 * Contains the vehicle's plate, model, and the user ID to which the vehicle belongs.
 */
public class CreateVehicleRequest {
    /**
     * The license plate of the vehicle.
     */
    private String plate;
    /**
     * The model of the vehicle.
     */
    private String model;
    /**
     * The ID of the user who owns the vehicle.
     */
    private Long userId;

    /**
     * Default constructor.
     */
    public CreateVehicleRequest() {}

    /**
     * Constructor with all fields.
     *
     * @param plate the license plate of the vehicle
     * @param model the model of the vehicle
     * @param userId the ID of the user who owns the vehicle
     */
    public CreateVehicleRequest(String plate, String model, Long userId) {
        this.plate = plate;
        this.model = model;
        this.userId = userId;
    }

    /**
     * Gets the license plate of the vehicle.
     * @return the plate
     */
    public String getPlate() { return plate; }
    /**
     * Sets the license plate of the vehicle.
     * @param plate the plate to set
     */
    public void setPlate(String plate) { this.plate = plate; }
    /**
     * Gets the model of the vehicle.
     * @return the model
     */
    public String getModel() { return model; }
    /**
     * Sets the model of the vehicle.
     * @param model the model to set
     */
    public void setModel(String model) { this.model = model; }
    /**
     * Gets the user ID of the vehicle owner.
     * @return the userId
     */
    public Long getUserId() { return userId; }
    /**
     * Sets the user ID of the vehicle owner.
     * @param userId the userId to set
     */
    public void setUserId(Long userId) { this.userId = userId; }
} 