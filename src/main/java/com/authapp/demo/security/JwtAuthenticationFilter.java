package com.authapp.demo.security;

import com.authapp.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

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
        
        // Load user details and set authentication
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            // Continue to controller
            filterChain.doFilter(request, response);
            
        } catch (UsernameNotFoundException e) {
            sendUnauthorizedResponse(response, "User not found");
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