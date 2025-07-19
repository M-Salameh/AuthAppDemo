package com.authapp.demo.controller;

import com.authapp.demo.repository.VehicleRepository;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.entity.Vehicle;
import com.authapp.demo.entity.User;
import com.authapp.demo.dto.CreateVehicleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import com.authapp.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<Vehicle> getVehiclesByUser(@PathVariable Long userId) {
        return vehicleRepository.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        return vehicle.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody CreateVehicleRequest request, @RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody CreateVehicleRequest request, @RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isAdmin(String authHeader) {
        return JwtUtil.isAdmin(authHeader);
    }
} 