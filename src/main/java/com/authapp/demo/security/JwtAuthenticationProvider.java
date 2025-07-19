package com.authapp.demo.security;

import com.authapp.demo.entity.User;
import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = authentication.getCredentials().toString();
        
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return new UsernamePasswordAuthenticationToken(
                    user,
                    token,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
            }
        }
        
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 