package com.authapp.demo;

import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import com.authapp.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@TestComponent
public class TestDataSetup {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void setupTestData() {
        // Create test users if they don't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setId(1L);
            adminUser.setUsername("admin");
            adminUser.setPassword("adminpass");
            adminUser.setRole(Role.ADMIN);
            userRepository.save(adminUser);
        }

        if (userRepository.findByUsername("user1").isEmpty()) {
            User regularUser = new User();
            regularUser.setId(2L);
            regularUser.setUsername("user1");
            regularUser.setPassword("userpass");
            regularUser.setRole(Role.USER);
            userRepository.save(regularUser);
        }

        if (userRepository.findByUsername("user2").isEmpty()) {
            User anotherUser = new User();
            anotherUser.setId(3L);
            anotherUser.setUsername("user2");
            anotherUser.setPassword("userpass");
            anotherUser.setRole(Role.USER);
            userRepository.save(anotherUser);
        }
    }
} 