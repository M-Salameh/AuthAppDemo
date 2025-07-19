package com.authapp.demo.controller;

import com.authapp.demo.repository.VehicleRepository;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.entity.Vehicle;
import com.authapp.demo.entity.User;
import com.authapp.demo.dto.CreateVehicleRequest;
import com.authapp.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing vehicles.
 * Provides endpoints for CRUD operations on vehicles, including admin-restricted actions.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    /**
     * Repository for vehicle data access.
     */
    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Repository for user data access.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * JWT utility component for token operations.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all vehicles in the system.
     *
     * @return list of all vehicles
     */
    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Get all vehicles belonging to a specific user.
     *
     * @param userId the ID of the user
     * @return list of vehicles for the user
     */
    @GetMapping("/user/{userId}")
    public List<Vehicle> getVehiclesByUser(@PathVariable Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    /**
     * Get a vehicle by its ID.
     *
     * @param id the ID of the vehicle
     * @return the vehicle if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        return vehicle.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new vehicle. Only accessible by admins.
     *
     * @param request the vehicle creation request containing plate, model, and userId
     * @param authHeader the Authorization header containing the JWT token
     * @return the created vehicle, or error if user not found or not admin
     */
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody CreateVehicleRequest request, @RequestHeader("Authorization") String authHeader) {
        if (!jwtUtil.isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(request.getPlate());
        vehicle.setModel(request.getModel());
        vehicle.setUser(userOpt.get());
        return ResponseEntity.ok(vehicleRepository.save(vehicle));
    }

    /**
     * Update an existing vehicle. Only accessible by admins.
     *
     * @param id the ID of the vehicle to update
     * @param request the vehicle update request containing plate, model, and userId
     * @param authHeader the Authorization header containing the JWT token
     * @return the updated vehicle, or error if not found, user not found, or not admin
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody CreateVehicleRequest request, @RequestHeader("Authorization") String authHeader) {
        if (!jwtUtil.isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicle.setPlate(request.getPlate());
                    vehicle.setModel(request.getModel());
                    vehicle.setUser(userOpt.get());
                    return ResponseEntity.ok(vehicleRepository.save(vehicle));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a vehicle by its ID. Only accessible by admins.
     *
     * @param id the ID of the vehicle to delete
     * @param authHeader the Authorization header containing the JWT token
     * @return 204 No Content if deleted, 404 if not found, or 403 if not admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        if (!jwtUtil.isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 