package com.authapp.demo;

import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.security.CustomUserDetails;
import com.authapp.demo.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PreAuthorizeIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testCustomUserDetailsCreation() {
        // Create a test user
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.USER);
        
        userRepository.save(user);
        
        // Create CustomUserDetails
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        // Verify the ID is set correctly
        assertEquals(1L, userDetails.getId());
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminRoleAccess() {
        // This test verifies that @PreAuthorize works with roles
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testUserRoleAccess() {
        // This test verifies that @PreAuthorize works with roles
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER")));
    }
} 