package com.authapp.demo.repository;

import com.authapp.demo.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for accessing Vehicle entities from the database.
 * Extends JpaRepository to provide CRUD operations and custom queries for Vehicle.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    /**
     * Finds all vehicles belonging to a specific user by user ID.
     *
     * @param userId the ID of the user
     * @return a list of vehicles owned by the user
     */
    List<Vehicle> findByUserId(Long userId);
} 