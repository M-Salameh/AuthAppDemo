package com.authapp.demo.security;

import com.authapp.demo.repository.UserRepository;
import com.authapp.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;


@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = authentication.getCredentials().toString();
        String username = authentication.getName();
        
        if (jwtUtil.validateToken(token)) {
            // Verify the username from token matches the requested username
            String tokenUsername = jwtUtil.extractUsername(token);
            if (!username.equals(tokenUsername)) {
                return null;
            }
            
            // Load user details and authorities
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            return new UsernamePasswordAuthenticationToken(
                userDetails,
                token,
                userDetails.getAuthorities()
            );
        }
        
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 