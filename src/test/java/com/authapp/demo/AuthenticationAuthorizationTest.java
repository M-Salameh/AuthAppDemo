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
class SimpleAuthenticationTest {

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
    void testValidJWT_AdminAccess_ShouldSucceed() {
        String token = jwtUtil.generateToken(adminUser);
        String authHeader = "Bearer " + token;

        assertTrue(jwtUtil.isAdmin(authHeader), "Admin user should have admin access");
        assertTrue(jwtUtil.hasRole(authHeader, "ADMIN"), "Admin user should have ADMIN role");
    }

    @Test
    void testValidJWT_UserAccessingAdminEndpoint_ShouldFail() {
        String token = jwtUtil.generateToken(regularUser);
        String authHeader = "Bearer " + token;

        assertFalse(jwtUtil.isAdmin(authHeader), "Regular user should not have admin access");
        assertTrue(jwtUtil.hasRole(authHeader, "USER"), "Regular user should have USER role");
    }

    @Test
    void testInvalidJWT_ShouldFail() {
        String invalidAuthHeader = "Bearer invalid.jwt.token";

        assertFalse(jwtUtil.isAdmin(invalidAuthHeader), "Invalid JWT should fail admin check");
        assertFalse(jwtUtil.hasRole(invalidAuthHeader, "ADMIN"), "Invalid JWT should fail role check");
    }

    @Test
    void testMissingJWT_ShouldFail() {
        String authHeader = null;

        assertFalse(jwtUtil.isAdmin(authHeader), "Missing JWT should fail admin check");
        assertFalse(jwtUtil.hasRole(authHeader, "ADMIN"), "Missing JWT should fail role check");
    }
} 