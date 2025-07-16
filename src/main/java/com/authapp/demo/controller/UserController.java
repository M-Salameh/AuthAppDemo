package com.authapp.demo.controller;

import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import com.authapp.demo.util.JwtUtil;
import org.springframework.web.bind.annotation.RequestHeader;
import com.authapp.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import com.authapp.demo.entity.User.Role;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    //private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestHeader("Authorization") String authHeader) {
        // TODO: Hash password before saving
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Admin access required");
        }
        // Hash password before saving
        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPassword(user.getPassword());
        // Ensure role is set from string if needed
        if (user.getRole() == null && user instanceof Map) {
            Object roleObj = ((Map<?, ?>)user).get("role");
            if (roleObj != null) user.setRole(Role.valueOf(roleObj.toString()));
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, @RequestHeader("Authorization") String authHeader) {
        return userRepository.findById(id)
                .map(user -> {
                    if (!isAdmin(authHeader) && !isSelf(authHeader, user.getUsername())) {
                        return ResponseEntity.status(403).body("Not authorized");
                    }
                    user.setUsername(userDetails.getUsername());
                   // user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    user.setPassword((userDetails.getPassword()));
                    user.setRole(userDetails.getRole());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        return userRepository.findById(id)
                .map(user -> {
                    if (!isAdmin(authHeader) && !isSelf(authHeader, user.getUsername())) {
                        return ResponseEntity.status(403).body("Not authorized");
                    }
                    userRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean isAdmin(String authHeader) {
        return JwtUtil.isAdmin(authHeader);
    }

    private boolean isSelf(String authHeader, String username) {
        return JwtUtil.isSelf(authHeader , username);
    }
} 