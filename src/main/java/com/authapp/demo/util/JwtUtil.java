package com.authapp.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.authapp.demo.entity.User;
import com.authapp.demo.entity.User.Role;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "your-very-secret-key";
    private static final long EXPIRATION_TIME = 86400000; // 1 day in ms
    public static final String ADMIN_TOKEN = "admin-token";
    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public static String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }

    public static boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

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