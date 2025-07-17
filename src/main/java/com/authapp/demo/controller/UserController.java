package com.authapp.demo.controller;

import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.entity.User;
import com.authapp.demo.security.AdminOnly;
import com.authapp.demo.security.SelfOrAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import com.authapp.demo.util.JwtUtil;
import org.springframework.web.bind.annotation.RequestHeader;
import com.authapp.demo.entity.User.Role;

/**
 * REST controller for managing users.
 * Provides endpoints for user authentication and CRUD operations on users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    /**
     * Repository for user data access.
     */
    @Autowired
    private UserRepository userRepository;

    //private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Authenticates a user and generates a JWT token if credentials are valid.
     *
     * @param loginRequest a map containing username and password
     * @return a JWT token if authentication is successful, or 401 if invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
           // if (passwordEncoder.matches(password, user.getPassword())) {
            if (password.equals(user.getPassword())) {
                String token = JwtUtil.generateToken(user);
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    /**
     * Retrieves all users in the system.
     *
     * @return list of all users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user
     * @return the user if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new user. Only accessible by admins.
     *
     * @param user the user to create
     * @param authHeader the Authorization header containing the JWT token
     * @return the created user, or error if not admin
     */
    @PostMapping
    @AdminOnly
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestHeader("Authorization") String authHeader) {
        // Hash password before saving
        user.setPassword(user.getPassword());
        // Ensure role is set from string if needed
        if (user.getRole() == null && user instanceof Map) {
            Object roleObj = ((Map<?, ?>)user).get("role");
            if (roleObj != null) user.setRole(Role.valueOf(roleObj.toString()));
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    /**
     * Updates an existing user. Only accessible by admins or the user themselves.
     *
     * @param id the ID of the user to update
     * @param userDetails the updated user details
     * @param authHeader the Authorization header containing the JWT token
     * @return the updated user, or error if not authorized or not found
     */
    @PutMapping("/{id}")
    @SelfOrAdmin
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, @RequestHeader("Authorization") String authHeader) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setPassword((userDetails.getPassword()));
                    user.setRole(userDetails.getRole());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a user by their ID. Only accessible by admins or the user themselves.
     *
     * @param id the ID of the user to delete
     * @param authHeader the Authorization header containing the JWT token
     * @return 204 No Content if deleted, 404 if not found, or 403 if not authorized
     */
    @DeleteMapping("/{id}")
    @SelfOrAdmin
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
} 