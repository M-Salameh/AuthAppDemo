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
    void testAdminToken() {
        // Create an admin user
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRole(Role.ADMIN);

        // Generate token
        String token = jwtUtil.generateToken(adminUser);
        String authHeader = "Bearer " + token;

        // Test admin check
        assertTrue(jwtUtil.isAdmin(authHeader));
    }

    @Test
    void testSelfCheck() {
        // Create a user
        User user = new User();
        user.setId(3L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.USER);

        // Generate token
        String token = jwtUtil.generateToken(user);
        String authHeader = "Bearer " + token;

        // Test self check
        assertTrue(jwtUtil.isSelf(authHeader, "testuser"));
        assertFalse(jwtUtil.isSelf(authHeader, "otheruser"));
    }

    @Test
    void testRoleCheck() {
        // Create a user with specific role
        User user = new User();
        user.setId(4L);
        user.setUsername("manager");
        user.setPassword("password");
        user.setRole(Role.ADMIN);

        // Generate token
        String token = jwtUtil.generateToken(user);
        String authHeader = "Bearer " + token;

        // Test role check
        assertTrue(jwtUtil.hasRole(authHeader, "ADMIN"));
        assertFalse(jwtUtil.hasRole(authHeader, "USER"));
    }
} 