package com.authapp.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import java.util.Date;

/**
 * Utility class for handling JWT (JSON Web Token) operations such as token generation,
 * validation, extraction of claims, and role checks for authentication and authorization.
 */
public class JwtUtil {
    /**
     * Secret key used for signing JWT tokens.
     */
    private static final String SECRET_KEY = "your-very-secret-key";
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
    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * Extracts claims from a JWT token.
     *
     * @param token the JWT token
     * @return the Claims object containing token data
     */
    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) from the token
     */
    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the role from a JWT token.
     *
     * @param token the JWT token
     * @return the role as a String
     */
    public static String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }

    /**
     * Validates a JWT token for a given user.
     *
     * @param token the JWT token
     * @param user the user to validate against
     * @return true if the token is valid and belongs to the user, false otherwise
     */
    public static boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * Checks if the user associated with the JWT token is an admin.
     *
     * @param authHeader the Authorization header containing the JWT token
     * @return true if the user is an admin, false otherwise
     */
    public static boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        if (token.equals(ADMIN_TOKEN)) {
            return true;
        }
        try {
            Claims claims = JwtUtil.extractClaims(token);
            return Role.ADMIN.name().equals(claims.get("role"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the user associated with the JWT token matches the given username.
     *
     * @param authHeader the Authorization header containing the JWT token
     * @param username the username to check
     * @return true if the username matches the token's subject, false otherwise
     */
    public static boolean isSelf(String authHeader, String username) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        try {
            Claims claims = JwtUtil.extractClaims(token);
            return username.equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }
} 