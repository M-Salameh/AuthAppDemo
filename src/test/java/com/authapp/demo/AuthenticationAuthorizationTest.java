package com.authapp.demo;

import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import com.authapp.demo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthenticationAuthorizationTest {

    @Autowired
    private JwtUtil jwtUtil;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("adminpass");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("user1");
        regularUser.setPassword("userpass");
        regularUser.setRole(Role.USER);
    }

    @Test
    void testValidJWT_AdminUser_ShouldSucceed() {
        String token = jwtUtil.generateToken(adminUser);
        
        assertTrue(jwtUtil.validateToken(token), "Admin user token should be valid");
        assertEquals("admin", jwtUtil.extractUsername(token), "Username should match");
        assertEquals("ADMIN", jwtUtil.extractRole(token), "Role should be ADMIN");
        assertTrue(jwtUtil.isTokenValid(token, adminUser), "Token should be valid for admin user");
    }

    @Test
    void testValidJWT_RegularUser_ShouldSucceed() {
        String token = jwtUtil.generateToken(regularUser);
        
        assertTrue(jwtUtil.validateToken(token), "Regular user token should be valid");
        assertEquals("user1", jwtUtil.extractUsername(token), "Username should match");
        assertEquals("USER", jwtUtil.extractRole(token), "Role should be USER");
        assertTrue(jwtUtil.isTokenValid(token, regularUser), "Token should be valid for regular user");
    }

    @Test
    void testInvalidJWT_ShouldFail() {
        String invalidToken = "invalid.jwt.token";

        assertFalse(jwtUtil.validateToken(invalidToken), "Invalid JWT should fail validation");
        assertNull(jwtUtil.extractUsername(invalidToken), "Invalid JWT should return null username");
        assertNull(jwtUtil.extractRole(invalidToken), "Invalid JWT should return null role");
    }

    @Test
    void testNullJWT_ShouldFail() {
        assertFalse(jwtUtil.validateToken(null), "Null JWT should fail validation");
        assertNull(jwtUtil.extractUsername(null), "Null JWT should return null username");
        assertNull(jwtUtil.extractRole(null), "Null JWT should return null role");
    }

    @Test
    void testTokenCrossValidation_ShouldFail() {
        String adminToken = jwtUtil.generateToken(adminUser);
        String userToken = jwtUtil.generateToken(regularUser);
        
        // Admin token should not be valid for regular user
        assertFalse(jwtUtil.isTokenValid(adminToken, regularUser), "Admin token should not be valid for regular user");
        
        // User token should not be valid for admin user
        assertFalse(jwtUtil.isTokenValid(userToken, adminUser), "User token should not be valid for admin user");
    }
} 