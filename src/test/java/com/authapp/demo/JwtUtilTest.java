package com.authapp.demo;

import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import com.authapp.demo.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateAndValidateToken() {
        // Create a test user
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.USER);

        // Generate token
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Validate token
        assertTrue(jwtUtil.validateToken(token));

        // Extract username
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("testuser", extractedUsername);

        // Extract role
        String extractedRole = jwtUtil.extractRole(token);
        assertEquals("USER", extractedRole);
    }

    @Test
    void testInvalidToken() {
        // Test with null token
        assertFalse(jwtUtil.validateToken(null));

        // Test with empty token
        assertFalse(jwtUtil.validateToken(""));

        // Test with invalid token
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void testTokenExpiration() {
        // Create a test user
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.USER);

        // Generate token
        String token = jwtUtil.generateToken(user);
        
        // Token should not be expired immediately
        assertFalse(jwtUtil.isTokenExpired(token));
        
        // Test with invalid token
        assertTrue(jwtUtil.isTokenExpired("invalid.token"));
    }

    @Test
    void testTokenValidationForUser() {
        // Create a test user
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.USER);

        // Generate token
        String token = jwtUtil.generateToken(user);
        
        // Token should be valid for the user
        assertTrue(jwtUtil.isTokenValid(token, user));
        
        // Test with different user
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");
        differentUser.setPassword("password");
        differentUser.setRole(Role.USER);
        
        assertFalse(jwtUtil.isTokenValid(token, differentUser));
    }
} 