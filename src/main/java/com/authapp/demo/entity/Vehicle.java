package com.authapp.demo.entity;

import com.authapp.demo.util.UserSummarySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.authapp.demo.util.VehicleSummarySerializer;
import jakarta.persistence.*;

/**
 * Entity representing a vehicle in the system.
 * Contains vehicle details and the associated user.
 */
@Entity
public class Vehicle {
    /**
     * The unique identifier for the vehicle.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The license plate of the vehicle.
     */
    @Column(nullable = false)
    private String plate;

    /**
     * The model of the vehicle.
     */
    @Column(nullable = false)
    private String model;

    /**
     * The user who owns the vehicle.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonSerialize(using = UserSummarySerializer.class)
    private User user;

    // Getters and setters
    /**
     * Gets the vehicle ID.
     * @return the vehicle ID
     */
    public Long getId() { return id; }
    /**
     * Sets the vehicle ID.
     * @param id the vehicle ID to set
     */
    public void setId(Long id) { this.id = id; }
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
     * Gets the user who owns the vehicle.
     * @return the user
     */
    public User getUser() { return user; }
    /**
     * Sets the user who owns the vehicle.
     * @param user the user to set
     */
    public void setUser(User user) { this.user = user; }
} 