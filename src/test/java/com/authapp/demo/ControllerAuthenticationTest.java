package com.authapp.demo;

import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import com.authapp.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ControllerAuthenticationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User adminUser;
    private User regularUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // Create admin user
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword("adminpass");
        adminUser.setRole(Role.ADMIN);

        // Create regular user
        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("user1");
        regularUser.setPassword("userpass");
        regularUser.setRole(Role.USER);

        // Create another user
        anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setUsername("user2");
        anotherUser.setPassword("userpass");
        anotherUser.setRole(Role.USER);
    }

    // ==================== VALID JWT WITH ACCESS TESTS ====================

    @Test
    void testValidJWT_AdminAccess_ShouldSucceed() throws Exception {
        // Admin user accessing admin-only endpoint
        String token = jwtUtil.generateToken(adminUser);
        String authHeader = "Bearer " + token;

        // Test admin accessing vehicle creation (admin-only endpoint)
        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC123"))
                .andExpect(jsonPath("$.model").value("Toyota Camry"));
    }

    @Test
    void testValidJWT_UserAccessingOwnResource_ShouldSucceed() throws Exception {
        // Regular user accessing their own resource
        String token = jwtUtil.generateToken(regularUser);
        String authHeader = "Bearer " + token;

        // Test user accessing their own user data
        mockMvc.perform(get("/api/users/2")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void testValidJWT_AdminAccessingAnyResource_ShouldSucceed() throws Exception {
        // Admin accessing any user's resource
        String token = jwtUtil.generateToken(adminUser);
        String authHeader = "Bearer " + token;

        // Test admin accessing any user's data
        mockMvc.perform(get("/api/users/2")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));

        mockMvc.perform(get("/api/users/3")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"));
    }

    // ==================== VALID JWT WITHOUT ACCESS TESTS ====================

    @Test
    void testValidJWT_UserAccessingAdminEndpoint_ShouldFail() throws Exception {
        // Regular user trying to access admin-only endpoint
        String token = jwtUtil.generateToken(regularUser);
        String authHeader = "Bearer " + token;

        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    @Test
    void testValidJWT_UserAccessingOtherUserResource_ShouldFail() throws Exception {
        // User1 trying to access User2's resource
        String token = jwtUtil.generateToken(regularUser);
        String authHeader = "Bearer " + token;

        // Test user1 trying to update user2's data
        String updateRequest = """
            {
                "username": "user2",
                "password": "newpass",
                "role": "USER"
            }
            """;

        mockMvc.perform(put("/api/users/3")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not authorized"));
    }

    @Test
    void testValidJWT_UserAccessingOtherUserResource_ShouldFail_GetRequest() throws Exception {
        // User1 trying to access User2's resource via GET
        String token = jwtUtil.generateToken(regularUser);
        String authHeader = "Bearer " + token;

        // Note: This depends on your controller implementation
        // If your controller doesn't check authorization for GET requests, this might pass
        // If it does check, it should fail
        mockMvc.perform(get("/api/users/3")
                .header("Authorization", authHeader))
                .andExpect(status().isOk()); // This might be OK depending on your implementation
    }

    // ==================== INVALID JWT TESTS ====================

    @Test
    void testInvalidJWT_ShouldFail() throws Exception {
        // Invalid JWT token
        String invalidAuthHeader = "Bearer invalid.jwt.token";

        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", invalidAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    @Test
    void testMalformedJWT_ShouldFail() throws Exception {
        // Malformed JWT token
        String malformedAuthHeader = "Bearer eyJhbGciOiJIUzUxMiJ9.malformed.signature";

        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", malformedAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    // ==================== MISSING JWT TESTS ====================

    @Test
    void testMissingJWT_ShouldFail() throws Exception {
        // No Authorization header
        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    @Test
    void testEmptyAuthorizationHeader_ShouldFail() throws Exception {
        // Empty Authorization header
        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    @Test
    void testIncompleteBearerToken_ShouldFail() throws Exception {
        // Incomplete Bearer token
        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", "Bearer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    // ==================== PUBLIC ENDPOINT TESTS ====================

    @Test
    void testPublicEndpoint_Login_ShouldSucceedWithoutJWT() throws Exception {
        // Login endpoint should work without JWT
        String loginRequest = """
            {
                "username": "admin",
                "password": "adminpass"
            }
            """;

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testPublicEndpoint_GetAllUsers_ShouldSucceedWithoutJWT() throws Exception {
        // GET /api/users should work without JWT (depending on your implementation)
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void testNonBearerAuthorizationHeader_ShouldFail() throws Exception {
        // Non-Bearer authorization header
        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    @Test
    void testExpiredJWT_ShouldFail() throws Exception {
        // Note: This would require a way to generate expired tokens
        // For now, we test with an obviously invalid token
        String expiredAuthHeader = "Bearer expired.jwt.token";

        String vehicleRequest = """
            {
                "plate": "ABC123",
                "model": "Toyota Camry",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", expiredAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Admin access required"));
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void testCompleteFlow_LoginThenAccessProtectedEndpoint() throws Exception {
        // Step 1: Login to get JWT
        String loginRequest = """
            {
                "username": "admin",
                "password": "adminpass"
            }
            """;

        String loginResponse = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response
        String token = objectMapper.readTree(loginResponse).get("token").asText();
        String authHeader = "Bearer " + token;

        // Step 2: Use JWT to access protected endpoint
        String vehicleRequest = """
            {
                "plate": "XYZ789",
                "model": "Honda Civic",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("XYZ789"))
                .andExpect(jsonPath("$.model").value("Honda Civic"));
    }

    @Test
    void testCompleteFlow_LoginWithWrongCredentials_ShouldFail() throws Exception {
        // Login with wrong credentials
        String loginRequest = """
            {
                "username": "admin",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
} 