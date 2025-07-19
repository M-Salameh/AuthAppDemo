package com.authapp.demo.controller;

import com.authapp.demo.repository.VehicleRepository;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.entity.Vehicle;
import com.authapp.demo.entity.User;
import com.authapp.demo.dto.CreateVehicleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing vehicles.
 * Provides endpoints for CRUD operations on vehicles, including admin-restricted actions.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all vehicles (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get vehicle by ID (admin or owner)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @vehicleRepository.findById(#id).orElse(null)?.getUser()?.getId() == authentication.principal.id")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        return vehicle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new vehicle (admin or user can create for themselves)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #request.userId == authentication.principal.id")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody CreateVehicleRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(request.getPlate());
        vehicle.setModel(request.getModel());
        vehicle.setUser(user.get());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(savedVehicle);
    }

    /**
     * Update vehicle (admin or owner)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @vehicleRepository.findById(#id).orElse(null)?.getUser()?.getId() == authentication.principal.id")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicleDetails) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Vehicle vehicle = vehicleOpt.get();
        vehicle.setPlate(vehicleDetails.getPlate());
        vehicle.setModel(vehicleDetails.getModel());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(updatedVehicle);
    }

    /**
     * Delete vehicle (admin or owner)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @vehicleRepository.findById(#id).orElse(null)?.getUser()?.getId() == authentication.principal.id")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get vehicles by owner ID (admin or self)
     */
    @GetMapping("/owner/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<Vehicle>> getVehiclesByOwner(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Vehicle> vehicles = vehicleRepository.findByUserId(userId);
        return ResponseEntity.ok(vehicles);
    }
} 