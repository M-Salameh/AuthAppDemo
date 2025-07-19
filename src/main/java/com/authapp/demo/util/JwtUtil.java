package com.authapp.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Spring component for handling JWT (JSON Web Token) operations such as token generation,
 * validation, extraction of claims, and role checks for authentication and authorization.
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    /**
     * Secret key used for signing JWT tokens.
     */
    private static final String SECRET_KEY =
            "CqIBTmqxJqQoJYZ4m7LaxX55PvZCoKDWeDNZb+vfSqVGJ7Jmrdbjb0K7FJCZkndRzuR0DSXaU91vWolIBx+9Rg==";
    
    /**
     * Expiration time for JWT tokens in milliseconds (1 day).
     */
    private static final long EXPIRATION_TIME = 86400000; // 1 day in ms
    
    /**
     * Special token value for admin access (for testing or legacy purposes).
     */
    public static final String ADMIN_TOKEN = "admin-token";

    /**
     * Generates a JWT token for the given user.
     *
     * @param user the user for whom to generate the token
     * @return the generated JWT token as a String
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            logger.warn("Token is null or empty");
            return false;
        }
        
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error validating JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token the JWT token
     * @return the Claims object containing token data, or null if invalid
     */
    public Claims extractClaims(String token) {
        if (!validateToken(token)) {
            return null;
        }
        
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) from the token, or null if invalid
     */
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extracts the role from a JWT token.
     *
     * @param token the JWT token
     * @return the role as a String, or null if invalid
     */
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * Validates a JWT token for a given user.
     *
     * @param token the JWT token
     * @param user the user to validate against
     * @return true if the token is valid and belongs to the user, false otherwise
     */
    public boolean isTokenValid(String token, User user) {
        if (user == null) {
            logger.warn("User is null");
            return false;
        }
        
        String username = extractUsername(token);
        if (username == null) {
            return false;
        }
        
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        Claims claims = extractClaims(token);
        if (claims == null) {
            return true; // Consider invalid tokens as expired
        }
        
        try {
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
} 