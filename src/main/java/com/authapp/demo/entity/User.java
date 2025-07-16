package com.authapp.demo.entity;

import com.authapp.demo.util.VehicleSummarySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.authapp.demo.util.UserSummarySerializer;
import jakarta.persistence.*;
import java.util.List;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Entity representing a user in the system.
 * Contains user credentials, role, and associated vehicles.
 */
@Entity
public class User {
    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique username of the user.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * The hashed password of the user.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The role of the user (ADMIN or USER).
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN or USER

    /**
     * The list of vehicles owned by the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonSerialize(contentUsing = VehicleSummarySerializer.class)
    private List<Vehicle> vehicles;

    /**
     * Enum representing user roles.
     */
    public enum Role {
        /**
         * Administrator role with elevated privileges.
         */
        ADMIN,
        /**
         * Standard user role.
         */
        USER
    }

    // Getters and setters
    /**
     * Gets the user ID.
     * @return the user ID
     */
    public Long getId() { return id; }
    /**
     * Sets the user ID.
     * @param id the user ID to set
     */
    public void setId(Long id) { this.id = id; }
    /**
     * Gets the username.
     * @return the username
     */
    public String getUsername() { return username; }
    /**
     * Sets the username.
     * @param username the username to set
     */
    public void setUsername(String username) { this.username = username; }
    /**
     * Gets the password.
     * @return the password
     */
    public String getPassword() { return password; }
    /**
     * Sets the password.
     * @param password the password to set
     */
    public void setPassword(String password) { this.password = password; }
    /**
     * Gets the user role.
     * @return the role
     */
    public Role getRole() { return role; }
    /**
     * Sets the user role.
     * @param role the role to set
     */
    public void setRole(Role role) { this.role = role; }
    /**
     * Gets the list of vehicles owned by the user.
     * @return the list of vehicles
     */
    public List<Vehicle> getVehicles() { return vehicles; }
    /**
     * Sets the list of vehicles owned by the user.
     * @param vehicles the list of vehicles to set
     */
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }
} 