package com.authapp.demo.security;

import com.authapp.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String requestURI = request.getRequestURI();
        
        // Allow public endpoints without authentication
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        
        // Check if Authorization header is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Missing authorization header");
            return;
        }
        
        String jwt = authHeader.substring(7);
        
        // Validate JWT token
        if (!jwtUtil.validateToken(jwt)) {
            sendUnauthorizedResponse(response, "Invalid or expired token");
            return;
        }
        
        // Extract username from token
        String username = jwtUtil.extractUsername(jwt);
        if (username == null) {
            sendUnauthorizedResponse(response, "Invalid token format");
            return;
        }
        
        try {
            // Use AuthenticationManager with JWT token
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, jwt, null);
            
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Continue to controller
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "Authentication failed: " + e.getMessage());
            return;
        }
    }
    
    /**
     * Determines if the endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String uri) {
        return uri.equals("/api/users/login");
    }
    
    /**
     * Sends an unauthorized response with a JSON error message
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"status\": 401}", 
            message
        );
        
        response.getWriter().write(jsonResponse);
    }
}  